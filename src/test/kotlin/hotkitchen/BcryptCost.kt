package hotkitchen

import org.mindrot.jbcrypt.BCrypt
import kotlin.test.Test

//Used to measure "right value" for bcrypt cost, it is measurement not a real test.
class BcryptCostTest {
    @Test
    fun `measure bcrypt cost`() {
        for (cost in 10..14) {
            val start = System.currentTimeMillis()
            BCrypt.hashpw("password123", BCrypt.gensalt(cost))
            println("cost $cost: ${System.currentTimeMillis() - start} ms")
        }
    }
}