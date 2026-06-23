package me.anderctrl.luteciokt

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.schlaubi.lavakord.kord.lavakord
import org.apache.logging.log4j.LogManager

suspend fun main() {
    val logger = LogManager.getLogger("Bot")
    val kord = Kord(System.getenv("DISCORD_TOKEN"))

    val lavalink = kord.lavakord {
    }
    lavalink.addNode("ws://localhost:2333", "lava")

    val commands = listOf(
        PlayCommand(lavalink),
        SkipCommand(lavalink),
        StopCommand(lavalink),
        PingCommand(),
    )

    logger.info("Registering ${commands.size} slash commands...")
    commands.forEach { command ->
        kord.createGuildChatInputCommand(
            Snowflake(954334832228442132),
            command.name,
            command.description,
            command.builder()
        )
    }

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val invokedCommand = commands.find { it.name == interaction.command.rootName }
        if (invokedCommand != null) {
            try {
                invokedCommand.execute(interaction, kord)
            } catch (e: Exception) {
                logger.error("Error executing command ${invokedCommand.name}", e)
            }
        }
    }

    logger.info("Starting bot...")
    kord.login()
}