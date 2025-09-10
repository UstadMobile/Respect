package world.respect.shared.domain.account.authwithpassword

import io.ktor.util.decodeBase64Bytes
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.libutil.ext.randomString
import world.respect.libxxhash.XXStringHasher
import world.respect.shared.domain.account.AuthResponse
import world.respect.datalayer.school.model.AuthToken
import world.respect.libutil.util.throwable.ForbiddenException
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


/**
 * @property schoolDb Uses the database directly because the SchoolDataSource itself requires
 *           an authenticated user. If this use case required a SchoolDataSource for authentication,
 *           this would create a chicken/egg scenario.
 */
class GetTokenAndUserProfileWithUsernameAndPasswordDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val xxHash: XXStringHasher,
): GetTokenAndUserProfileWithUsernameAndPasswordUseCase {

    override suspend fun invoke(
        username: String,
        password: String
    ): AuthResponse {
        val personEntity = schoolDb.getPersonEntityDao().findByUsername(username)
            ?: throw IllegalArgumentException()
        val personGuidHash = xxHash.hash(personEntity.person.pGuid)
        val personPassword = schoolDb.getPersonPasswordEntityDao().findByUid(personGuidHash)
            ?: throw ForbiddenException("Invalid username/password")

        val keySpec = PBEKeySpec(password.toCharArray(), personPassword.authSalt.toByteArray(),
            personPassword.authIterations, personPassword.authKeyLen)
        val keyFactory = SecretKeyFactory.getInstance(personPassword.authAlgorithm)
        val expectedAuthEncoded = personPassword.authEncoded.decodeBase64Bytes()
        val actualAuthEncoded = keyFactory.generateSecret(keySpec).encoded

        if (expectedAuthEncoded.contentEquals(actualAuthEncoded)) {
            val token = AuthToken(
                accessToken = randomString(32),
                timeCreated = System.currentTimeMillis(),
                ttl = TOKEN_DEFAULT_TTL,
            )

            schoolDb.getAuthTokenEntityDao().insert(
                token.toEntity(personEntity.person.pGuid, personGuidHash)
            )

            return AuthResponse(
                token = token,
                person = personEntity.toPersonEntities().toModel(),
            )
        }else {
            throw ForbiddenException("Invalid username/password")
        }
    }

    companion object {

        const val TOKEN_DEFAULT_TTL = (60 * 60 * 24 * 365)//one year

    }
}