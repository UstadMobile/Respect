package world.respect.shared.domain.account.authwithpassword

import io.ktor.http.Url
import io.ktor.util.decodeBase64Bytes
import world.respect.credentials.passkey.RespectCredential
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.ext.dataOrNull
import world.respect.libutil.ext.randomString
import world.respect.libxxhash.XXStringHasher
import world.respect.shared.domain.account.AuthResponse
import world.respect.datalayer.school.model.AuthToken
import world.respect.libutil.util.throwable.ForbiddenException
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.passkey.VerifySignInWithPasskeyUseCase
import java.lang.IllegalStateException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


/**
 * @property schoolDb Uses the database directly because the SchoolDataSource itself requires
 *           an authenticated user. If this use case required a SchoolDataSource for authentication,
 *           this would create a chicken/egg scenario.
 */
class GetTokenAndUserProfileWithCredentialDbImpl(
    private val schoolUrl: Url,
    private val schoolDb: RespectSchoolDatabase,
    private val xxHash: XXStringHasher,
    private val verifyPasskeyUseCase: VerifySignInWithPasskeyUseCase?,
    private val respectAppDataSource: RespectAppDataSource,
): GetTokenAndUserProfileWithCredentialUseCase {

    override suspend fun invoke(
        credential: RespectCredential,
    ): AuthResponse {

        val personEntity = when(credential) {
            is RespectPasswordCredential -> {
                val personEntity = schoolDb.getPersonEntityDao().findByUsername(credential.username)
                    ?: throw IllegalArgumentException()
                val personGuidHash = xxHash.hash(personEntity.person.pGuid)
                val personPassword = schoolDb.getPersonPasswordEntityDao().findByUid(personGuidHash)
                    ?: throw ForbiddenException("Invalid username/password")

                val keySpec = PBEKeySpec(
                    credential.password.toCharArray(),
                    personPassword.authSalt.toByteArray(),
                    personPassword.authIterations,
                    personPassword.authKeyLen
                )
                val keyFactory = SecretKeyFactory.getInstance(personPassword.authAlgorithm)
                val expectedAuthEncoded = personPassword.authEncoded.decodeBase64Bytes()
                val actualAuthEncoded = keyFactory.generateSecret(keySpec).encoded

                if (expectedAuthEncoded.contentEquals(actualAuthEncoded)) {
                    personEntity
                }else {
                    throw ForbiddenException("Invalid username/password")
                }
            }

            is RespectPasskeyCredential -> {
                val rpId = respectAppDataSource.schoolDirectoryEntryDataSource
                    .getSchoolDirectoryEntryByUrl(schoolUrl).dataOrNull()?.rpId
                    ?: throw IllegalStateException("School $schoolUrl has no rpId")
                        .withHttpStatus(400)

                val verifyPasskeyUseCaseVal = verifyPasskeyUseCase
                    ?: throw IllegalStateException("Verify passkey use case not provided")

                verifyPasskeyUseCaseVal(
                    credential.passkeyWebAuthNResponse,
                    rpId = rpId
                )
                val passkeyId = credential.passkeyWebAuthNResponse.id
                val personPasskey = schoolDb.getPersonPasskeyEntityDao().findPersonPasskeyFromClientDataJson(
                    passkeyId
                ) ?: throw IllegalArgumentException().withHttpStatus(400)

                schoolDb.getPersonEntityDao().findByGuidNum(
                    personPasskey.ppPersonUidNum
                ) ?: throw ForbiddenException("Person not found")
            }
        }

        val token = AuthToken(
            accessToken = randomString(32),
            timeCreated = System.currentTimeMillis(),
            ttl = TOKEN_DEFAULT_TTL,
        )

        val personGuidHash = xxHash.hash(personEntity.person.pGuid)
        schoolDb.getAuthTokenEntityDao().insert(
            token.toEntity(personEntity.person.pGuid, personGuidHash)
        )

        return AuthResponse(
            token = token,
            person = personEntity.toPersonEntities().toModel(),
        )

    }

    companion object {

        const val TOKEN_DEFAULT_TTL = (60 * 60 * 24 * 365)//one year

    }
}