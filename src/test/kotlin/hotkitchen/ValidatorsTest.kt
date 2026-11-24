package hotkitchen

import hotkitchen.validation.isValidEmail
import hotkitchen.validation.isValidPassword
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatorsTest {

    @Test
    fun `accepts a normal email`() {
        assertTrue(isValidEmail("example@mail.com"))
    }

    @Test
    fun `rejects an email without @`() {
        assertFalse(isValidEmail("example.gmail.com"))
    }

    @Test
    fun `accepts a password with letters and digits`() {
        assertTrue(isValidPassword("password123"))
    }

    @Test
    fun `rejects a password without digits`() {
        assertFalse(isValidPassword("password"))
    }

    @Test
    fun `rejects a password shorter than six characters`() {
        assertFalse(isValidPassword("abc12"))
    }
}