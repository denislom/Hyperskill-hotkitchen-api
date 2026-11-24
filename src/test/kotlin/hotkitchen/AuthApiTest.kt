package hotkitchen

import hotkitchen.models.TokenResponse
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthApiTest {

//    private fun apiTest(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
//        environment { config = ApplicationConfig("application.yaml") }
//        val client = createClient { install(ContentNegotiation) { json() } }
//        block(client)
//    }

//    private fun uniqueEmail() = "test-${UUID.randomUUID()}@mail.com"

//    private suspend fun HttpClient.signUp(email: String, password: String = "password123") =
//        post("/signup") {
//            contentType(ContentType.Application.Json)
//            setBody(SignUpRequest(email, "client", password))
//        }

    @Test
    fun `signup returns a token`() = apiTest { client ->
        val response = client.signUp(uniqueEmail())

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.body<TokenResponse>().token.isNotBlank())
    }

    @Test
    fun `signup with the same email twice is forbidden`() = apiTest { client ->
        val email = uniqueEmail()
        client.signUp(email)

        val second = client.signUp(email)

        assertEquals(HttpStatusCode.Forbidden, second.status)
        assertTrue(second.bodyAsText().contains("User already exists"))
    }

    @Test
    fun `validate without a token returns 401`() = apiTest { client ->
        val response = client.get("/validate")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `validate with a token greets the user`() = apiTest { client ->
        val email = uniqueEmail()
        val token = client.signUp(email).body<TokenResponse>().token

        val response = client.get("/validate") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, client $email", response.bodyAsText())
    }
}