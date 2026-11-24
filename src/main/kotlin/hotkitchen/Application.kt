package hotkitchen

import hotkitchen.plugins.configureDatabases
import hotkitchen.plugins.configureRouting
import hotkitchen.plugins.configureSecurity
import hotkitchen.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureRouting()
    configureDatabases()
}