package me.anderctrl.luteciokt

import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.embed

class PingCommand : SlashCommand {
    override val name = "ping"
    override val description = "Checks the bot's response latency"

    override suspend fun execute(interaction: ChatInputCommandInteraction, kord: Kord) {
        val gatewayPing = kord.gateway.averagePing?.inWholeMilliseconds ?: 0

        val thumbnailUrl =
            "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExb2sybGt5MnpwdXBhanZzMzhlMWNsbmxjNDgzb2F0NHNmcmtmbzlmMSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9cw/8P7ugGf2prBbDtQ3tk/giphy.gif"

        interaction.respondPublic {
            embed {
                title = "Pong!"
                color = Color(0x2ecc71)

                thumbnail {
                    url = thumbnailUrl
                }

                field {
                    name = "📡 Gateway Latency"
                    value = "`$gatewayPing ms`"
                    inline = true
                }
            }
        }
    }
}