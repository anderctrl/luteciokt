package me.anderctrl.luteciokt

import dev.kord.core.Kord
import dev.schlaubi.lavakord.LavaKord
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.count
import kotlinx.html.*

fun Kord.startStatusDashboard(lavalink: LavaKord, port: Int = 8000) {
    embeddedServer(Netty, port = port) {
        routing {
            staticResources("/static", "static")

            get("/api/status") {
                val pingValue = this@startStatusDashboard.gateway.averagePing?.inWholeMilliseconds
                val serverCount = this@startStatusDashboard.guilds.count()
                val isLavalinkConnected = lavalink.nodes.any { it.available }

                val jsonResponse = """
                    {
                        "ping": ${pingValue?.let { "\"${it}ms\"" } ?: "\"Calculating...\""},
                        "servers": $serverCount,
                        "lavalinkConnected": $isLavalinkConnected,
                        "lavalinkText": "${if (isLavalinkConnected) "Connected" else "Disconnected"}"
                    }
                """.trimIndent()

                call.respondText(jsonResponse, contentType = io.ktor.http.ContentType.Application.Json)
            }

            get("/") {
                val pingValue = this@startStatusDashboard.gateway.averagePing?.inWholeMilliseconds
                val serverCount = this@startStatusDashboard.guilds.count()

                val isLavalinkConnected = lavalink.nodes.any { it.available }
                val lavalinkStatus = if (isLavalinkConnected) "Connected" else "Disconnected"
                val lavalinkClass = if (isLavalinkConnected) "status-green" else "status-red"

                val selfUser = this@startStatusDashboard.getSelf()
                val botAvatarUrl = selfUser.avatar?.cdnUrl?.toUrl()

                call.respondHtml {
                    head {
                        title { +"${selfUser.username} Dashboard" }
                        link(rel = "stylesheet", href = "/static/style.css", type = "text/css")
                        link(rel = "icon", href = botAvatarUrl, type = "image/png")
                        link(rel = "shortcut icon", href = botAvatarUrl, type = "image/png")
                    }
                    body {
                        video(classes = "bg-video") {
                            attributes["autoplay"] = "true"
                            attributes["loop"] = "true"
                            attributes["muted"] = "true"
                            attributes["playsinline"] = "true"
                            source {
                                src = "/static/background.webm"
                                type = "video/webm"
                            }
                        }

                        div(classes = "card") {
                            div(classes = "brand-header") {
                                img(classes = "bot-avatar", src = botAvatarUrl, alt = "Bot Avatar")
                                h1 { +selfUser.username }
                            }

                            div(classes = "grid") {
                                div(classes = "metric") {
                                    span { +"Gateway Ping" }
                                    span(classes = "highlight") {
                                        id = "ping-val"
                                        +(pingValue?.let { "${it}ms" } ?: "Calculating...")
                                    }
                                }
                                div(classes = "metric") {
                                    span { +"Total Servers" }
                                    span(classes = "highlight") {
                                        id = "servers-val"
                                        +"$serverCount"
                                    }
                                }
                                div(classes = "metric") {
                                    span { +"Lavalink Status" }
                                    span(classes = lavalinkClass) {
                                        id = "lavalink-val"
                                        +lavalinkStatus
                                    }
                                }
                            }
                        }

                        script(src = "/static/dashboard.js") {}
                    }
                }
            }
        }
    }.start(wait = false)
}