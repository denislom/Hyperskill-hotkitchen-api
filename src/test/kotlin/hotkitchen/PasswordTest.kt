package hotkitchen

import hotkitchen.security.hashPassword
import hotkitchen.security.verifyPassword
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordsTest {

    @Test
    fun `hash is not the plain password`() {
        assertNotEquals("password123", hashPassword("password123"))
    }

    @Test
    fun `same password gives different hashes because of the salt`() {
        assertNotEquals(hashPassword("password123"), hashPassword("password123"))
    }

    @Test
    fun `correct password verifies`() {
        val hash = hashPassword("password123")
        assertTrue(verifyPassword("password123", hash))
    }

    @Test
    fun `wrong password does not verify`() {
        val hash = hashPassword("password123")
        assertFalse(verifyPassword("wrongpass1", hash))
    }

    @Test
    fun `old plain-text password does not crash`() {
        assertFalse(verifyPassword("password123", "password123"))
    }
}