package hotkitchen.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date

class JwtSettings(
    secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expireInMs: Long
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun generateToken(email: String, userType: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("email", email)
            .withClaim("userType", userType)
            .withExpiresAt(Date(System.currentTimeMillis() + expireInMs))
            .sign(algorithm)

    companion object {
        fun from(config: ApplicationConfig): JwtSettings {
            val secret = config.property("jwt.secret").getString()
            val issuer = config.property("jwt.issuer").getString()
            val audience = config.property("jwt.audience").getString()
            val realm = config.property("jwt.realm").getString()
            val expireInMs = config.property("jwt.expiresInMs").getString().toLong()
            return JwtSettings(secret, issuer, audience, realm, expireInMs)
        }
    }
}