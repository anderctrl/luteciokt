package me.anderctrl.luteciokt

import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.getLink

class StopCommand(private val lavalink: LavaKord) : SlashCommand {
    override val name = "stop"
    override val description = "Stops the music, clears the queue, and leaves the channel"

    override suspend fun execute(interaction: ChatInputCommandInteraction, kord: Kord) {
        val guildId = interaction.data.guildId.value ?: return
        val link = lavalink.getLink(guildId)

        GuildQueueManager.clearQueue(guildId.value)
        link.player.stopTrack()
        link.disconnectAudio()

        interaction.respondPublic {
            embed {
                applyTemplate(
                    interaction = interaction,
                    embedTitle = "🛑 Playback Stopped",
                    embedDescription = "Stopped playback, cleared the queue, and left the voice channel.",
                    embedColor = Color(0xe74c3c)
                )
            }
        }
    }
}