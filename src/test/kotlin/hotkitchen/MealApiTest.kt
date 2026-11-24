package hotkitchen

import hotkitchen.models.Meal
import hotkitchen.models.StatusResponse
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MealApiTest {

    private fun newMeal() = Meal(
        mealId = uniqueId(),
        title = "Pizza Margherita",
        price = 9.5f,
        imageUrl = "https://example.com/pizza.png",
        categoryIds = listOf(1, 2),
    )

    @Test
    fun `client cannot add a meal`() = apiTest { client ->
        val clientToken = client.signUpAndGetToken(uniqueEmail())

        val response = client.postMeal(clientToken, newMeal())

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(StatusResponse("Access denied"), response.body<StatusResponse>())
    }

    @Test
    fun `staff can add a meal`() = apiTest { client ->
        val staffToken = client.signUpAndGetToken(uniqueEmail(), userType = "staff")
        val meal = newMeal()

        val response = client.postMeal(staffToken, meal)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(meal, response.body<Meal>())
    }

    @Test
    fun `adding the same meal twice returns 400`() = apiTest { client ->
        val staffToken = client.signUpAndGetToken(uniqueEmail(), userType = "staff")
        val meal = newMeal()
        client.postMeal(staffToken, meal)

        val second = client.postMeal(staffToken, meal)

        assertEquals(HttpStatusCode.BadRequest, second.status)
    }

    @Test
    fun `get meal by id returns the stored meal`() = apiTest { client ->
        val staffToken = client.signUpAndGetToken(uniqueEmail(), userType = "staff")
        val clientToken = client.signUpAndGetToken(uniqueEmail())
        val meal = newMeal()
        client.postMeal(staffToken, meal)

        val response = client.get("/meals?id=${meal.mealId}") { bearerAuth(clientToken) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(meal, response.body<Meal>())
    }

    @Test
    fun `get meal with unknown id returns 400`() = apiTest { client ->
        val token = client.signUpAndGetToken(uniqueEmail())

        val response = client.get("/meals?id=-1") { bearerAuth(token) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `get meals lists the new meal`() = apiTest { client ->
        val staffToken = client.signUpAndGetToken(uniqueEmail(), userType = "staff")
        val meal = newMeal()
        client.postMeal(staffToken, meal)

        val response = client.get("/meals") { bearerAuth(staffToken) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(meal in response.body<List<Meal>>())
    }
}