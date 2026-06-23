package me.anderctrl.luteciokt

import dev.kord.common.Color
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import kotlin.time.Clock

fun EmbedBuilder.applyTemplate(
    interaction: ChatInputCommandInteraction,
    embedTitle: String,
    embedDescription: String,
    embedColor: Color
) {
    title = embedTitle
    description = embedDescription
    color = embedColor
    timestamp = Clock.System.now()

    footer {
        text = "Executed by ${interaction.user.username}"
        icon = interaction.user.avatar?.cdnUrl?.toUrl()
    }
}

fun EmbedBuilder.applySystemTemplate(
    embedTitle: String,
    embedDescription: String,
    embedColor: Color
) {
    title = embedTitle
    description = embedDescription
    color = embedColor
    timestamp = Clock.System.now()

    footer {
        text = "System"
    }
}