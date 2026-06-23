package me.anderctrl.luteciokt

import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackExceptionEvent
import dev.schlaubi.lavakord.audio.TrackStuckEvent
import dev.schlaubi.lavakord.audio.on
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

object GuildQueueManager {
    private val queues = ConcurrentHashMap<ULong, LinkedBlockingQueue<Track>>()
    private val channelIds = ConcurrentHashMap<ULong, Snowflake>()

    private val locks = ConcurrentHashMap<ULong, Mutex>()

    fun getQueue(guildId: ULong): LinkedBlockingQueue<Track> {
        return queues.computeIfAbsent(guildId) { LinkedBlockingQueue() }
    }

    private fun getLock(guildId: ULong): Mutex {
        return locks.computeIfAbsent(guildId) { Mutex() }
    }

    fun setChannelId(guildId: ULong, channelId: Snowflake) {
        channelIds[guildId] = channelId
    }

    fun clearQueue(guildId: ULong) {
        queues[guildId]?.clear()
        channelIds.remove(guildId)
        locks.remove(guildId)
    }

    fun registerPlayerEvents(link: Link, guildId: ULong, kord: Kord) {
        link.player.on<TrackEndEvent> {
            if (reason.mayStartNext || reason == Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.STOPPED) {
                kord.launch {
                    playNext(link, guildId, kord)
                }
            }
        }

        link.player.on<TrackExceptionEvent> {
            val textChannelId = channelIds[guildId]
            if (textChannelId != null) {
                kord.launch {
                    try {
                        kord.getChannelOf<dev.kord.core.entity.channel.TextChannel>(textChannelId)
                            ?.createMessage {
                                embed {
                                    title = "⚠️ Playback Error"
                                    description =
                                        "Could not load video data (YouTube script error). Moving to next song..."
                                    color = Color(0xe67e22)
                                }
                            }
                    } catch (_: Exception) {
                    }

                    playNext(link, guildId, kord)
                }
            }
        }

        link.player.on<TrackStuckEvent> {
            kord.launch {
                playNext(link, guildId, kord)
            }
        }
    }

    suspend fun playNext(link: Link, guildId: ULong, kord: Kord): Track? {
        return getLock(guildId).withLock {
            val queue = getQueue(guildId)
            val nextTrack = queue.poll()

            if (nextTrack != null) {
                link.player.playTrack(nextTrack)

                val upNextTrack = queue.peek()
                val textChannelId = channelIds[guildId]

                if (textChannelId != null) {
                    kord.launch {
                        try {
                            kord.getChannelOf<dev.kord.core.entity.channel.TextChannel>(textChannelId)
                                ?.createMessage {
                                    embed {
                                        applySystemTemplate(
                                            embedTitle = "🎧 Now Playing",
                                            embedDescription = "Playing next from queue:\n[${nextTrack.info.title}](${nextTrack.info.uri})",
                                            embedColor = Color(0x2ecc71)
                                        )
                                        thumbnail { url = nextTrack.info.artworkUrl ?: "" }

                                        field {
                                            name = "⏭️ Up Next"
                                            value = if (upNextTrack != null) {
                                                "[${upNextTrack.info.title}](${upNextTrack.info.uri})"
                                            } else {
                                                "Nothing else in queue!"
                                            }
                                            inline = false
                                        }
                                    }
                                }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            nextTrack
        }
    }
}