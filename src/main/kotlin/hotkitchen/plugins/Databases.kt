package hotkitchen.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table("users") {
    val email = varchar("email", 255)
    val userType = varchar("user_type", 255)
    val password = varchar("password", 255)
    override val primaryKey = PrimaryKey(email)
}

object Profiles: Table("profiles") {
    val email = varchar("email", 255).references(Users.email, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val userType = varchar("user_type", 255)
    val phone = varchar("phone", 255)
    val address = varchar("address", 500)
    override val primaryKey = PrimaryKey(email)
}

object Meals: Table("meals") {
    val mealId = integer("meal_id")
    val title = varchar("title", 255)
    val price = float("price")
    val imageUrl = varchar("image_url", 255)
    override val primaryKey = PrimaryKey(mealId)
}

object MealCategories : Table("meal_categories") {
    val mealId = integer("meal_id").references(Meals.mealId, onDelete = ReferenceOption.CASCADE)
    val categoryId = integer("category_id")
//    override val primaryKey = PrimaryKey(mealId, categoryId)
}

object Categories: Table("categories") {
    val categoryId = integer("category_id")
    val title = varchar("title", 255)
    val description = varchar("description", 100)
    override val primaryKey = PrimaryKey(categoryId)
}

object Orders : Table("orders") {
    val orderId = integer("order_id").autoIncrement()
    val userEmail = varchar("user_email", 255)
    val mealsIds = array<Int>("meals_ids")
    val price = float("price")
    val address = varchar("address", 500)
    val status = varchar("status", 20)
    override val primaryKey = PrimaryKey(orderId)
}

fun Application.configureDatabases() {
    val config = environment.config
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.property("database.url").getString()
        driverClassName = config.property("database.driver").getString()
        username = config.property("database.user").getString()
        password = config.property("database.password").getString()
        maximumPoolSize = 3
    })
    monitor.subscribe(ApplicationStopped) {dataSource.close()}

    Database.connect(dataSource)
    transaction { SchemaUtils.create(Users, Profiles, Meals, MealCategories, Categories, Orders)}
    }