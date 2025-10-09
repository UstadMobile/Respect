package world.respect.shared.domain.account.setpassword

import io.ktor.util.encodeBase64
import world.respect.datalayer.school.model.PersonPassword
import world.respect.libutil.ext.randomString
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.time.Clock

class EncryptPersonPasswordUseCaseImpl: EncryptPersonPasswordUseCase {

    override fun invoke(request: EncryptPersonPasswordUseCase.Request): PersonPassword {
        val salt = randomString(DEFAULT_SALT_LEN)

        val keySpec = PBEKeySpec(request.password.toCharArray(), salt.toByteArray(),
            DEFAULT_ITERATIONS,
            DEFAULT_KEY_LEN
        )
        val keyFactory = SecretKeyFactory.getInstance(KEY_ALGO)

        val now = Clock.System.now()

        return PersonPassword(
            personGuid = request.personGuid,
            authAlgorithm = KEY_ALGO,
            authEncoded = keyFactory.generateSecret(keySpec).encoded.encodeBase64(),
            authSalt = salt,
            authIterations = DEFAULT_ITERATIONS,
            authKeyLen = DEFAULT_KEY_LEN,
            lastModified = now,
            stored = now,
        )
    }



    companion object {

        const val DEFAULT_SALT_LEN = 16

        const val DEFAULT_ITERATIONS = 10_000

        const val DEFAULT_KEY_LEN = 512

        const val KEY_ALGO = "PBKDF2WithHmacSHA1"

    }
}