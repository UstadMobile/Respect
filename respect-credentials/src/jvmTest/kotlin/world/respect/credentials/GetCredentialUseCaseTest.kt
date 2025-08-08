package world.respect.credentials

import io.ktor.http.Url
import world.respect.credentials.passkey.username.CreateCredentialUsernameUseCase
import world.respect.credentials.passkey.username.ParseCredentialUsernameUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCredentialUseCaseTest {

    private fun assertConvertedAsExpected(
        username: String,
        url:Url,
        expectedCredentialUsername: String
    ) {
        val createCredentialUsernameUseCase = CreateCredentialUsernameUseCase(url)
        val credentialUsername = createCredentialUsernameUseCase(username = username)
        assertEquals(expectedCredentialUsername, credentialUsername)
        val (convertedLearningSpace, convertedUsername) = ParseCredentialUsernameUseCase().invoke(credentialUsername)

        assertEquals(username, convertedUsername)
        assertEquals(url, convertedLearningSpace)
    }

    @Test
    fun givenHttpsLearningSpace_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("janedoe",
            Url("https://learningspace.example.org/"),
            "janedoe@learningspace.example.org"
        )
    }

    @Test
    fun givenHttpsLearningSpaceWithPath_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("bobdoe",
            Url("https://learningspace.example.org/path/"),
            "bobdoe@learningspace.example.org/path"
        )
    }

    @Test
    fun givenPlainHttpLearningSpace_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("bobdoe",
            Url("http://learningspace.example.org/"),
            "bobdoe@http://learningspace.example.org/"
        )
    }

    @Test
    fun givenPlainHttpLearningSpaceWithPath_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("bobdoe",
            Url("http://learningspace.example.org/path/"),
            "bobdoe@http://learningspace.example.org/path/"
        )
    }

}