package world.respect.credentials.passkey.password

/**
 * This use case will save the username and password to the any password manager
 * available in your device later during login popup will be shown for easy login.
 */
interface SavePasswordUseCase {

    suspend operator fun invoke(username:String, password: String)

}