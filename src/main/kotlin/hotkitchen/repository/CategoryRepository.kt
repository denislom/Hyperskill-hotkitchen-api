package hotkitchen.repository

import hotkitchen.models.Category
import hotkitchen.plugins.Categories
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

fun insertCategory(category: Category) {
    Categories.insert {
        it[categoryId] = category.categoryId
        it[title] = category.title
        it[description] = category.description
    }
}

fun findCategory(id: Int): Category? {
    val row = Categories.selectAll().where { Categories.categoryId eq id }.singleOrNull() ?: return null
    return row.toCategory()
}

fun allCategories(): List<Category> = Categories.selectAll().map { it.toCategory() }

private fun ResultRow.toCategory(): Category =
    Category(
        categoryId = this[Categories.categoryId],
        title = this[Categories.title],
        description = this[Categories.description],
    )