package hotkitchen.repository

import hotkitchen.models.Order
import hotkitchen.models.OrderStatus
import hotkitchen.plugins.Meals
import hotkitchen.plugins.Orders
import hotkitchen.plugins.Profiles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

fun createOrder(email: String, mealIds: List<Int>): Order? {
    if (mealIds.isEmpty()) return null

    val prices = Meals.selectAll()
        .where { Meals.mealId inList mealIds.distinct() }
        .associate { it[Meals.mealId] to it[Meals.price] }
    if (prices.size != mealIds.distinct().size) return null

    val address = Profiles.selectAll()
        .where { Profiles.email eq email }
        .singleOrNull()
        ?.get(Profiles.address) ?: return null

    val price = mealIds.sumOf { prices.getValue(it).toDouble() }.toFloat()

    val orderId = Orders.insert {
        it[userEmail] = email
        it[mealsIds] = mealIds
        it[Orders.price] = price
        it[Orders.address] = address
        it[status] = OrderStatus.IN_PROGRESS
    }[Orders.orderId]

    return Order(orderId, email, mealIds, price, address, OrderStatus.IN_PROGRESS)
}

fun markOrderComplete(id: Int): Order? {
    val updated = Orders.update({ Orders.orderId eq id }) {
        it[status] = OrderStatus.COMPLETE
    }
    if (updated == 0) return null
    return Orders.selectAll().where { Orders.orderId eq id }.single().toOrder()
}

fun allOrders(): List<Order> = Orders.selectAll().map { it.toOrder() }

fun incompleteOrders(): List<Order> =
    Orders.selectAll().where { Orders.status eq OrderStatus.IN_PROGRESS }.map { it.toOrder() }

private fun ResultRow.toOrder(): Order =
    Order(
        orderId = this[Orders.orderId],
        userEmail = this[Orders.userEmail],
        mealsIds = this[Orders.mealsIds],
        price = this[Orders.price],
        address = this[Orders.address],
        status = this[Orders.status],
    )