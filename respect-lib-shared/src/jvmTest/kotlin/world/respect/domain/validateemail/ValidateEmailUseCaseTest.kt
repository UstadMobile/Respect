package world.respect.domain.validateemail

import world.respect.shared.domain.validateemail.ValidateEmailUseCase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateEmailUseCaseTest {

    private val validateEmailUseCase = ValidateEmailUseCase()

    @Test
    fun `valid email should return true`() {
        assertTrue(validateEmailUseCase("test@example.com"))
        assertTrue(validateEmailUseCase("user.name@domain.co.in"))
        assertTrue(validateEmailUseCase("a@b.io"))
    }

    @Test
    fun `email without at symbol should return false`() {
        assertFalse(validateEmailUseCase("testexample.com"))
    }

    @Test
    fun `email without dot after at should return false`() {
        assertFalse(validateEmailUseCase("test@examplecom"))
    }

    @Test
    fun `email with spaces should return false`() {
        assertFalse(validateEmailUseCase("test @example.com"))
        assertFalse(validateEmailUseCase("test@ example.com"))
    }

    @Test
    fun `email with invalid characters should return false`() {
        assertFalse(validateEmailUseCase("test@[example].com"))
        assertFalse(validateEmailUseCase("test\\example@example.com"))
    }

    @Test
    fun `email with leading and trailing spaces should be valid`() {
        assertTrue(validateEmailUseCase("   test@example.com  "))
    }
}
