package me.anderctrl.luteciokt

import dev.kord.core.Kord
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.schlaubi.lavakord.kord.lavakord
import org.apache.logging.log4j.LogManager

suspend fun main() {
    val logger = LogManager.getLogger("Bot")
    val kord = Kord(System.getenv("DISCORD_TOKEN"))

    val lavalink = kord.lavakord {}

    try {
        lavalink.addNode(
            serverUri = "ws://${System.getenv("LAVALINK_URL")}",
            password = System.getenv("LAVALINK_PASS")
        )
        logger.info("Successfully registered Lavalink node connection framework.")
    } catch (e: Exception) {
        logger.error("⚠Failed to connect to Lavalink server node. Music features will be unavailable.", e)
    }

    kord.startStatusDashboard(lavalink = lavalink, port = System.getenv("DASHBOARD_PORT").toInt())

    val commands = listOf(
        PlayCommand(lavalink),
        SkipCommand(lavalink),
        StopCommand(lavalink),
        PingCommand(),
    )

    logger.info("Registering ${commands.size} slash commands...")
    commands.forEach { command ->

        kord.createGlobalChatInputCommand(command.name, command.description, command.builder())
        logger.info("Slash command '${command.name}' registered")
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