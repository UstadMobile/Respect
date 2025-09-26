package world.respect.shared.domain.account.gettokenanduser

import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.school.model.AuthToken
import world.respect.libutil.ext.randomString
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.AuthResponse
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl.Companion.TOKEN_DEFAULT_TTL
import java.lang.IllegalStateException

class GetTokenAndUserProfileWithPasskeyUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase
) : GetTokenAndUserProfileWithPasskeyUseCase{

    override suspend fun invoke(
        authJson: AuthenticationResponseJSON
    ): AuthResponse {
        val passkeyId = authJson.id
        val personPasskey = schoolDb.getPersonPasskeyEntityDao().findPersonPasskeyFromClientDataJson(
            passkeyId
        ) ?: throw IllegalArgumentException().withHttpStatus(400)

        val token = AuthToken(
            accessToken = randomString(32),
            timeCreated = System.currentTimeMillis(),
            ttl = TOKEN_DEFAULT_TTL,
        )

        val personEntity = schoolDb.getPersonEntityDao().findByGuidNum(
            personPasskey.ppPersonUid
        ) ?: throw IllegalStateException("Person not found")

        schoolDb.getAuthTokenEntityDao().insert(
            token.toEntity(personEntity.person.pGuid, personPasskey.ppPersonUid)
        )

        return AuthResponse(
            token = token,
            person = personEntity.toPersonEntities().toModel(),
        )
    }
}