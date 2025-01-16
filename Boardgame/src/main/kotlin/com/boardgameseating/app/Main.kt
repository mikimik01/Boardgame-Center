package org.example.app

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.boardgameseating.app.ApplicationConfig.module

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}