package me.anderctrl.luteciokt

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

object GuildQueueManager {
    private val queues = ConcurrentHashMap<ULong, LinkedBlockingQueue<Track>>()
    private val channelIds = ConcurrentHashMap<ULong, Snowflake>()

    fun getQueue(guildId: ULong): LinkedBlockingQueue<Track> {
        return queues.computeIfAbsent(guildId) { LinkedBlockingQueue() }
    }

    fun setChannelId(guildId: ULong, channelId: Snowflake) {
        channelIds[guildId] = channelId
    }

    fun clearQueue(guildId: ULong) {
        queues[guildId]?.clear()
        channelIds.remove(guildId)
    }

    fun startListening(link: Link, guildId: ULong, kord: Kord) {
        link.player.on<TrackEndEvent> {
            if (reason.mayStartNext) {
                playNext(link, guildId, kord)
            }
        }
    }

    suspend fun playNext(link: Link, guildId: ULong, kord: Kord): Track? {
        val queue = getQueue(guildId)
        val nextTrack = queue.poll()

        if (nextTrack != null) {
            link.player.playTrack(nextTrack)

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
                                }
                            }
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return nextTrack
    }
}