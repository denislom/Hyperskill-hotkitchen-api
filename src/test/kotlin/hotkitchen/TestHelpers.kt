package hotkitchen

import hotkitchen.models.Meal
import hotkitchen.models.SignUpRequest
import hotkitchen.models.TokenResponse
import hotkitchen.models.UserProfile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID
import kotlin.random.Random

object TestDatabase {
    val postgres = PostgreSQLContainer<Nothing>("postgres:17").apply { start() }
}
fun apiTest(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
    val db = TestDatabase.postgres
    environment {
        config = ApplicationConfig("application.yaml").mergeWith(
            MapApplicationConfig(
                "database.url" to db.jdbcUrl,
                "database.user" to db.username,
                "database.password" to db.password
            )
        )
    }
    val client = createClient { install(ContentNegotiation) { json() } }
    block(client)
}

fun uniqueEmail() = "test-${UUID.randomUUID()}@mail.com"

suspend fun HttpClient.signUp(
    email: String,
    password: String = "password123",
    userType: String = "client",
): HttpResponse =
    post("/signup") {
        contentType(ContentType.Application.Json)
        setBody(SignUpRequest(email, userType, password))
    }

suspend fun HttpClient.signUpAndGetToken(email: String, userType: String = "client"): String =
    signUp(email, userType = userType).body<TokenResponse>().token

fun uniqueId(): Int = Random.nextInt(1, Int.MAX_VALUE)

suspend fun HttpClient.postMeal(token: String, meal: Meal): HttpResponse =
    post("/meals") {
        bearerAuth(token)
        contentType(ContentType.Application.Json)
        setBody(meal)
    }

fun profileFor(email: String) = UserProfile(
    name = "Goose",
    userType = "client",
    phone = "+79999999999",
    email = email,
    address = "Main Street 1",
)

suspend fun HttpClient.putProfile(token: String, profile: UserProfile): HttpResponse =
    put("/me") {
        bearerAuth(token)
        contentType(ContentType.Application.Json)
        setBody(profile)
    }