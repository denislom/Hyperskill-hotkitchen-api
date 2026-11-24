package hotkitchen.repository

import hotkitchen.models.Meal
import hotkitchen.plugins.MealCategories
import hotkitchen.plugins.MealCategories.categoryId
import hotkitchen.plugins.Meals
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

fun insertMeal(meal: Meal) {
    Meals.insert {
        it[mealId] = meal.mealId
        it[title] = meal.title
        it[price] = meal.price
        it[imageUrl] = meal.imageUrl
    }
    MealCategories.batchInsert(meal.categoryIds) {
        catId ->
        this[MealCategories.mealId] = meal.mealId
        this[MealCategories.categoryId] = catId
    }
}

fun findMeal(id: Int): Meal? {
    val row = Meals.selectAll().where { Meals.mealId eq id }.singleOrNull() ?: return null
    return row.toMeal()
}

fun allMeal(): List<Meal> = Meals.selectAll().map { it.toMeal() }
private fun ResultRow.toMeal(): Meal {
    val id = this[Meals.mealId]
    val categoryIds = MealCategories.selectAll()
        .where { MealCategories.mealId eq id }
        .map { it[categoryId] }
    return Meal(
        mealId = id,
        title = this[Meals.title],
        price = this[Meals.price],
        imageUrl = this[Meals.imageUrl],
        categoryIds = categoryIds
    )
}
