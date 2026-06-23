package me.anderctrl.luteciokt

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.rest.loadItem

class PlayCommand(private val lavalink: LavaKord) : SlashCommand {
    override val name = "play"
    override val description = "Plays a song or adds it to the queue"

    override fun builder(): ChatInputCreateBuilder.() -> Unit = {
        string("query", "The song title or URL") { required = true }
    }

    override suspend fun execute(interaction: ChatInputCommandInteraction, kord: Kord) {
        val guildId = interaction.data.guildId.value ?: return
        val query = interaction.command.options["query"]?.value.toString()

        val member = interaction.user.asMember(guildId)
        val voiceState = member.getVoiceStateOrNull()
        val channelId = voiceState?.channelId

        if (channelId == null) {
            interaction.respondPublic {
                embed {
                    title = "❌ Voice Connection Error"
                    description = "You must be in a voice channel to use this command!"
                    color = Color(0xe74c3c)
                }
            }
            return
        }

        val ack = interaction.deferPublicResponse()
        val link = lavalink.getLink(guildId)

        GuildQueueManager.setChannelId(guildId.value, interaction.channelId)

        if (link.player.playingTrack == null && GuildQueueManager.getQueue(guildId.value).isEmpty()) {
            GuildQueueManager.registerPlayerEvents(link, guildId.value, kord)
        }

        link.connectAudio(channelId.value)

        val search = if (query.startsWith("http")) query else "ytsearch:$query"

        when (val item = link.loadItem(search)) {
            is LoadResult.TrackLoaded -> {
                handleTrack(item.data, link, guildId.value, ack, interaction)
            }

            is LoadResult.PlaylistLoaded -> {
                val tracks = item.data.tracks
                val queue = GuildQueueManager.getQueue(guildId.value)

                tracks.forEach { queue.add(it) }

                if (link.player.playingTrack == null) {
                    GuildQueueManager.playNext(link, guildId.value, kord)
                    ack.respond {
                        embed {
                            applyTemplate(
                                interaction,
                                "🎶 Playlist Loaded",
                                "Loaded playlist. Now playing: **[${tracks.first().info.title}](${tracks.first().info.uri})**",
                                Color(0x2ecc71)
                            )
                        }
                    }
                } else {
                    ack.respond {
                        embed {
                            applyTemplate(
                                interaction,
                                "🎶 Playlist Queued",
                                "Added **${tracks.size}** tracks from playlist to the queue.",
                                Color(0xe67e22)
                            )
                        }
                    }
                }
            }

            is LoadResult.SearchResult -> {
                val track = item.data.tracks.firstOrNull()
                if (track == null) {
                    ack.respond {
                        embed {
                            title = "❌ No Results"
                            description = "No matches found for your search query."
                            color = Color(0xe74c3c)
                        }
                    }
                    return
                }
                handleTrack(track, link, guildId.value, ack, interaction)
            }

            is LoadResult.NoMatches -> {
                ack.respond {
                    embed {
                        applyTemplate(
                            interaction,
                            "❌ No Matches",
                            "Could not find any playable matches for that input.",
                            Color(0xe74c3c)
                        )
                    }
                }
            }

            is LoadResult.LoadFailed -> {
                ack.respond {
                    embed {
                        applyTemplate(interaction, "❌ Failed to Load Track", "${item.data.message}", Color(0xe74c3c))
                    }
                }
            }
        }
    }

    private suspend fun handleTrack(
        track: Track,
        link: dev.schlaubi.lavakord.audio.Link,
        guildIdRaw: ULong,
        ack: dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior,
        interaction: ChatInputCommandInteraction
    ) {
        val queue = GuildQueueManager.getQueue(guildIdRaw)

        if (link.player.playingTrack == null) {
            link.player.playTrack(track)
            ack.respond {
                embed {
                    applyTemplate(
                        interaction, "🎧 Now Playing", "[${track.info.title}](${track.info.uri})", Color(0x2ecc71)
                    )
                    thumbnail { url = track.info.artworkUrl ?: "" }
                }
            }
        } else {
            queue.add(track)
            ack.respond {
                embed {
                    applyTemplate(
                        interaction, "⏳ Added to Queue", "[${track.info.title}](${track.info.uri})", Color(0xe67e22)
                    )
                    thumbnail { url = track.info.artworkUrl ?: "" }
                    field { name = "Position"; value = "${queue.size}"; inline = true }
                }
            }
        }
    }
}