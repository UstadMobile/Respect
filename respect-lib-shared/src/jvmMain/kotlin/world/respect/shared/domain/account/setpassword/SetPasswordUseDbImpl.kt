package world.respect.shared.domain.account.setpassword

import io.ktor.util.encodeBase64
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.entities.PersonPasswordEntity
import world.respect.libutil.ext.randomString
import world.respect.libxxhash.XXStringHasher
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.libutil.util.throwable.ForbiddenException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.text.toCharArray
import kotlin.time.Clock

class SetPasswordUseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val xxHash: XXStringHasher,
): SetPasswordUseCase {

    override suspend fun invoke(request: SetPasswordUseCase.SetPasswordRequest) {
        if(request.authenticatedUserId.guid == AuthenticatedUserPrincipalId.DIRECTORY_ADMIN_GUID ||
            request.userGuid == request.authenticatedUserId.guid
        ) {
            val salt = randomString(DEFAULT_SALT_LEN)

            val keySpec = PBEKeySpec(request.password.toCharArray(), salt.toByteArray(),
                DEFAULT_ITERATIONS, DEFAULT_KEY_LEN)

            val keyFactory = SecretKeyFactory.getInstance(KEY_ALGO)

            val now = Clock.System.now()
            schoolDb.getPersonPasswordEntityDao().upsert(
                PersonPasswordEntity(
                    ppwGuid = request.userGuid,
                    ppwGuidNum = xxHash.hash(request.userGuid),
                    authAlgorithm = KEY_ALGO,
                    authEncoded = keyFactory.generateSecret(keySpec).encoded.encodeBase64(),
                    authSalt = salt,
                    authIterations = DEFAULT_ITERATIONS,
                    authKeyLen = DEFAULT_KEY_LEN,
                    ppwStored = now,
                    ppwLastModified = now,
                )
            )
        }else {
            throw ForbiddenException("Authenticated user not allowed to set password for this user")
        }
    }

    companion object {

        const val DEFAULT_SALT_LEN = 16

        const val DEFAULT_ITERATIONS = 10_000

        const val DEFAULT_KEY_LEN = 512

        const val KEY_ALGO = "PBKDF2WithHmacSHA1"

    }
}