package me.anderctrl.luteciokt

import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.getLink

class SkipCommand(private val lavalink: LavaKord) : SlashCommand {
    override val name = "skip"
    override val description = "Skips the currently playing song"

    override suspend fun execute(interaction: ChatInputCommandInteraction, kord: Kord) {
        val guildId = interaction.data.guildId.value ?: return
        val link = lavalink.getLink(guildId)

        if (link.player.playingTrack == null) {
            interaction.respondPublic {
                embed {
                    applyTemplate(
                        interaction = interaction,
                        embedTitle = "❌ Skip Failed",
                        embedDescription = "There is no song playing right now!",
                        embedColor = Color(0xe74c3c)
                    )
                }
            }
            return
        }
        GuildQueueManager.playNext(link, guildId.value, kord)
        interaction.respondPublic {
            embed {
                applyTemplate(
                    interaction = interaction,
                    embedTitle = "⏭️ Track Skipped",
                    embedDescription = "Skipped the current song.",
                    embedColor = Color(0x2ecc71)
                )
            }
        }
    }
}