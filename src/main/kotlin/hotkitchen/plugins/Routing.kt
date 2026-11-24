package hotkitchen.plugins

import hotkitchen.models.Category
import hotkitchen.models.Meal
import hotkitchen.models.SigInRequest
import hotkitchen.models.SignUpRequest
import hotkitchen.models.StatusResponse
import hotkitchen.models.TokenResponse
import hotkitchen.models.UserProfile
import hotkitchen.plugins.Users.password
import hotkitchen.repository.allCategories
import hotkitchen.repository.allMeal
import hotkitchen.repository.allOrders
import hotkitchen.repository.createOrder
import hotkitchen.repository.findCategory
import hotkitchen.repository.findMeal
import hotkitchen.repository.incompleteOrders
import hotkitchen.repository.insertCategory
import hotkitchen.repository.insertMeal
import hotkitchen.repository.markOrderComplete
import hotkitchen.security.JwtSettings
import hotkitchen.security.hashPassword
import hotkitchen.security.verifyPassword
import hotkitchen.validation.isValidEmail
import hotkitchen.validation.isValidPassword
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert


fun Application.configureRouting() {
    val jwt = JwtSettings.from(environment.config)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/signup") {
            val request = call.receive<SignUpRequest>()

            if(!isValidEmail(request.email)) {
                call.respond(HttpStatusCode.Forbidden, StatusResponse("Invalid email"))
                return@post
            }
            if(!isValidPassword(request.password)) {
                call.respond(HttpStatusCode.Forbidden, StatusResponse("Invalid password"))
                return@post
            }

            val created = transaction {
                val exists = !Users.selectAll().where { Users.email eq request.email }.empty()
                if (!exists) {
                    Users.insert {
                        it[email] = request.email
                        it[userType] = request.userType
                        it[password] = hashPassword(request.password)
                    }
                }
                !exists
            }

            if (created) {
                call.respond(HttpStatusCode.OK, TokenResponse(jwt.generateToken(request.email,
                    request.userType)))
            } else {
                call.respond(HttpStatusCode.Forbidden, StatusResponse("User already exists"))
            }
        }

        post("/signin") {
            val request = call.receive<SigInRequest>()

            val userType = transaction {
                Users.selectAll()
                    .where { (Users.email eq request.email) }
                    .singleOrNull()
                    ?.takeIf { row -> verifyPassword(request.password, row[Users.password]) }
                    ?.get(Users.userType)
            }

            if (userType != null) {
                call.respond(HttpStatusCode.OK, TokenResponse(jwt.generateToken(request.email,
                    userType)))
            } else {
                call.respond(HttpStatusCode.Forbidden, StatusResponse("Invalid email or password"))
            }
        }

        authenticate("auth-jwt") {
            get("/validate") {
                val principal = call.principal<JWTPrincipal>()!!
                val email = principal.payload.getClaim("email").asString()
                val userType = principal.payload.getClaim("userType").asString()
                call.respondText("Hello, $userType $email")
            }

            get("/me") {
                val email = call.principal<JWTPrincipal>()!!.payload.getClaim("email").asString()

                val profile = transaction {
                    Profiles.selectAll()
                        .where { Profiles.email eq email }
                        .singleOrNull()
                        ?.let { row ->
                            UserProfile(
                                name = row[Profiles.name],
                                userType = row[Profiles.userType],
                                phone = row[Profiles.phone],
                                email = row[Profiles.email],
                                address = row[Profiles.address]
                            )
                        }
                }
                if (profile == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    call.respond(HttpStatusCode.OK, profile)
                }
            }

            put("/me") {
                val tokenEmail = call.principal<JWTPrincipal>()!!.payload.getClaim("email").asString()
                val profile = call.receive<UserProfile>()

                if (profile.email != tokenEmail) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }

                transaction {
                    Profiles.upsert {
                        it[email] = profile.email
                        it[name] = profile.name
                        it[userType] = profile.userType
                        it[Profiles.phone] = profile.phone
                        it[Profiles.address] = profile.address
                    }
                }

                call.respond(HttpStatusCode.OK, profile)
            }

            delete("/me") {
                val email = call.principal<JWTPrincipal>()!!.payload.getClaim("email").asString()

                val deleted = transaction {
                    Profiles.deleteWhere { Profiles.email eq email }
                    Users.deleteWhere { Users.email eq email }
                }

                if (deleted > 0) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post("/meals") {
                val userType = call.principal<JWTPrincipal>()!!.payload.getClaim("userType").asString()
                if (userType != "staff") {
                    call.respond(HttpStatusCode.Forbidden, StatusResponse("Access denied"))
                    return@post
                }

                val meal = call.receive<Meal>()
                val added = try {
                    transaction { insertMeal(meal) }
                    true
                } catch (e: Exception) {
                    false
                }

                if (added) {
                    call.respond(HttpStatusCode.OK, meal)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/meals") {
                val idParam = call.request.queryParameters["id"]

                if (idParam == null) {
                    call.respond(HttpStatusCode.OK, transaction {allMeal()})
                    return@get
                }

                val meal = idParam.toIntOrNull()?.let { id-> transaction { findMeal(id)}}
                if (meal == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    call.respond(HttpStatusCode.OK, meal)
                }
            }

            post("/categories") {
                val userType = call.principal<JWTPrincipal>()!!.payload.getClaim("userType").asString()
                if (userType != "staff") {
                    call.respond(HttpStatusCode.Forbidden, StatusResponse("Access denied"))
                    return@post
                }

                val category = call.receive<Category>()
                val added = try {
                    transaction { insertCategory(category) }
                    true
                } catch (e: Exception) {
                    false
                }

                if (added) {
                    call.respond(HttpStatusCode.OK, category)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/categories") {
                val idParam = call.request.queryParameters["id"]

                if (idParam == null) {
                    call.respond(HttpStatusCode.OK, transaction { allCategories() })
                    return@get
                }

                val category = idParam.toIntOrNull()?.let { id -> transaction { findCategory(id) } }
                if (category == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    call.respond(HttpStatusCode.OK, category)
                }
            }

            post("/order") {
                val email = call.principal<JWTPrincipal>()!!.payload.getClaim("email").asString()
                val mealIds = call.receive<List<Int>>()

                val order = transaction { createOrder(email, mealIds) }
                if (order == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    call.respond(HttpStatusCode.OK, order)
                }
            }

            post("/order/{orderId}/markReady") {
                val userType = call.principal<JWTPrincipal>()!!.payload.getClaim("userType").asString()
                if (userType != "staff") {
                    call.respond(HttpStatusCode.Forbidden, StatusResponse("Access denied"))
                    return@post
                }

                val order = call.parameters["orderId"]?.toIntOrNull()
                    ?.let { id -> transaction { markOrderComplete(id) } }
                if (order == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    call.respond(HttpStatusCode.OK, order)
                }
            }

            get("/orderHistory") {
                call.respond(HttpStatusCode.OK, transaction { allOrders() })
            }

            get("/orderIncomplete") {
                call.respond(HttpStatusCode.OK, transaction { incompleteOrders() })
            }
        }
    }
}