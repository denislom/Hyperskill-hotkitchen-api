package hotkitchen

import com.auth0.jwt.JWT
import hotkitchen.security.JwtSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class JwtSettingsTest {

    private val settings = JwtSettings("test-secret", "test-issuer", "test-audience",
        "realm", 60_000)

    @Test
    fun `token contains email and userType claims`() {
        val token = settings.generateToken("a@mail.com", "staff")

        val decoded = JWT.require(settings.algorithm)
            .withIssuer("test-issuer")
            .withAudience("test-audience")
            .build()
            .verify(token)

        assertEquals("a@mail.com", decoded.getClaim("email").asString())
        assertEquals("staff", decoded.getClaim("userType").asString())
    }
}