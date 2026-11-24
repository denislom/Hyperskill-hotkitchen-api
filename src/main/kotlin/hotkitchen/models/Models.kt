package hotkitchen.models

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(val email: String, val userType: String, val password: String)

@Serializable
data class StatusResponse(val status: String)

@Serializable
data class SigInRequest(val email: String, val password: String)

@Serializable
data class TokenResponse(val token: String)

@Serializable
data class UserProfile(
    val name: String,
    val userType: String,
    val phone: String,
    val email: String,
    val address: String,
)

@Serializable
data class Meal(
    val mealId: Int,
    val title: String,
    val price: Float,
    val imageUrl: String,
    val categoryIds: List<Int>,
)

@Serializable
data class Category(
    val categoryId: Int,
    val title: String,
    val description: String,
)

@Serializable
data class Order(
    val orderId: Int,
    val userEmail: String,
    val mealsIds: List<Int>,
    val price: Float,
    val address: String,
    val status: String,
)

object OrderStatus {
    const val IN_PROGRESS = "IN PROGRESS"
    const val COMPLETE = "COMPLETE"
}