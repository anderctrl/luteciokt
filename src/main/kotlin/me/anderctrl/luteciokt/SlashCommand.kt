package me.anderctrl.luteciokt

import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

interface SlashCommand {
    val name: String
    val description: String
    fun builder(): ChatInputCreateBuilder.() -> Unit = {}
    suspend fun execute(interaction: ChatInputCommandInteraction, kord: Kord)
}