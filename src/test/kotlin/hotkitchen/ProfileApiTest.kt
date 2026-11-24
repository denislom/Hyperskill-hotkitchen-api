package hotkitchen

import hotkitchen.models.UserProfile
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileApiTest {
    @Test
    fun `get me without a profile returns 400`() = apiTest { client ->
        val token = client.signUpAndGetToken(uniqueEmail())

        val response = client.get("/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `put me then get me returns the same profile`() = apiTest { client ->
        val email = uniqueEmail()
        val token = client.signUpAndGetToken(email)
        val profile = profileFor(email)

        val putResponse = client.putProfile(token, profile)
        val getResponse = client.get("/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, putResponse.status)
        assertEquals(profile, putResponse.body<UserProfile>())
        assertEquals(HttpStatusCode.OK, getResponse.status)
        assertEquals(profile, getResponse.body<UserProfile>())
    }

    @Test
    fun `put me updates an existing profile`() = apiTest { client ->
        val email = uniqueEmail()
        val token = client.signUpAndGetToken(email)
        client.putProfile(token, profileFor(email))
        val updated = profileFor(email).copy(name = "New Name", address = "New Address")

        client.putProfile(token, updated)
        val response = client.get("/me") { bearerAuth(token) }

        assertEquals(updated, response.body<UserProfile>())
    }

    @Test
    fun `put me with another email returns 400 and changes nothing`() = apiTest { client ->
        val email = uniqueEmail()
        val token = client.signUpAndGetToken(email)
        client.putProfile(token, profileFor(email))

        val response = client.putProfile(token, profileFor("someone-else@mail.com"))
        val stored = client.get("/me") { bearerAuth(token) }.body<UserProfile>()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(email, stored.email)
    }

    @Test
    fun `delete me twice returns 200 then 404`() = apiTest { client ->
        val token = client.signUpAndGetToken(uniqueEmail())

        val first = client.delete("/me") { bearerAuth(token) }
        val second = client.delete("/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, first.status)
        assertEquals(HttpStatusCode.NotFound, second.status)
    }
}