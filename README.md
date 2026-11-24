# HotKitchen API

REST API for a food-ordering service: user accounts with JWT authentication,
profiles, a staff-managed menu, and order processing.

Built as a [Hyperskill](https://hyperskill.org) project and extended with
BCrypt password hashing, integration tests and Testcontainers.

## Tech stack
Kotlin · Ktor 3 · Exposed · PostgreSQL 17 · HikariCP · JWT · BCrypt · Docker · Testcontainers

## Running locally
1. Start the database: `docker compose up -d`
2. Start the server: `./gradlew run` → http://localhost:28852

## Running tests
`./gradlew test` (requires Docker; a throwaway PostgreSQL is started automatically)

## API
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /signup | – | Register, returns a JWT |
| POST | /signin | – | Sign in, returns a JWT |
| GET | /validate | Bearer | Check a token |
| GET / PUT / DELETE | /me | Bearer | Read, create/update, delete own profile/account |
| GET | /meals, /meals?id= | Bearer | List meals / get one |
| POST | /meals | Bearer (staff) | Add a meal |
| GET | /categories, /categories?id= | Bearer | List categories / get one |
| POST | /categories | Bearer (staff) | Add a category |
| POST | /order | Bearer | Place an order |
| POST | /order/{id}/markReady | Bearer (staff) | Complete an order |
| GET | /orderHistory, /orderIncomplete | Bearer | List orders |

## Example
    curl -X POST http://localhost:28852/signup \
      -H "Content-Type: application/json" \
      -d '{"email":"chef@mail.com","userType":"staff","password":"password123"}'

## Security notes
- Passwords are hashed with BCrypt and never stored in plain text.
- Secrets are read from environment variables (`JWT_SECRET`, `DB_PASSWORD`, …).