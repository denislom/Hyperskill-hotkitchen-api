package hotkitchen

import hotkitchen.models.Meal
import hotkitchen.models.Order
import hotkitchen.models.OrderStatus
import hotkitchen.models.StatusResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderApiTest {

    private class Setup(
        val staffToken: String,
        val clientToken: String,
        val clientEmail: String,
        val meals: List<Meal>,
    )

    private suspend fun HttpClient.setUp(): Setup {
        val staffToken = signUpAndGetToken(uniqueEmail(), userType = "staff")
        val clientEmail = uniqueEmail()
        val clientToken = signUpAndGetToken(clientEmail)
        putProfile(clientToken, profileFor(clientEmail))

        val meals = listOf(
            Meal(uniqueId(), "Pizza", 9.5f, "https://example.com/pizza.png", listOf(1)),
            Meal(uniqueId(), "Soup", 4.25f, "https://example.com/soup.png", listOf(2)),
        )
        meals.forEach { postMeal(staffToken, it) }

        return Setup(staffToken, clientToken, clientEmail, meals)
    }

    private suspend fun HttpClient.postOrder(token: String, mealIds: List<Int>): HttpResponse =
        post("/order") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(mealIds)
        }

    @Test
    fun `order with an unknown meal id returns 400`() = apiTest { client ->
        val s = client.setUp()

        val response = client.postOrder(s.clientToken, listOf(s.meals[0].mealId, -1))

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `order without a profile returns 400`() = apiTest { client ->
        val s = client.setUp()
        val tokenWithoutProfile = client.signUpAndGetToken(uniqueEmail())

        val response = client.postOrder(tokenWithoutProfile, listOf(s.meals[0].mealId))

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `order price is the sum of the meal prices`() = apiTest { client ->
        val s = client.setUp()
        val mealIds = s.meals.map { it.mealId }

        val response = client.postOrder(s.clientToken, mealIds)
        val order = response.body<Order>()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(13.75f, order.price)
        assertEquals(mealIds, order.mealsIds)
        assertEquals(s.clientEmail, order.userEmail)
        assertEquals("Main Street 1", order.address)
        assertEquals(OrderStatus.IN_PROGRESS, order.status)
    }

    @Test
    fun `client cannot mark an order as ready`() = apiTest { client ->
        val s = client.setUp()
        val order = client.postOrder(s.clientToken, listOf(s.meals[0].mealId)).body<Order>()

        val response = client.post("/order/${order.orderId}/markReady") { bearerAuth(s.clientToken) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(StatusResponse("Access denied"), response.body<StatusResponse>())
    }

    @Test
    fun `staff can mark an order as ready`() = apiTest { client ->
        val s = client.setUp()
        val order = client.postOrder(s.clientToken, listOf(s.meals[0].mealId)).body<Order>()

        val response = client.post("/order/${order.orderId}/markReady") { bearerAuth(s.staffToken) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(order.copy(status = OrderStatus.COMPLETE), response.body<Order>())
    }
}