package hotkitchen.plugins

import com.auth0.jwt.JWT
import hotkitchen.security.JwtSettings
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond


fun Application.configureSecurity() {
    val settings = JwtSettings.from(environment.config)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = settings.realm
            verifier(
                JWT.require(settings.algorithm)
                    .withIssuer(settings.issuer)
                    .withAudience(settings.audience)
                    .build()
            )
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                val userType = credential.payload.getClaim("userType").asString()
                if (!email.isNullOrBlank() && !userType.isNullOrBlank())
                    JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}