package world.respect.sharedse.domain.account.authenticatepassword

import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import io.ktor.util.decodeBase64Bytes
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.libutil.util.throwable.ForbiddenException

class AuthenticatePasswordUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
    private val uidNumberMapper: UidNumberMapper,
) : AuthenticatePasswordUseCase {

    override suspend fun invoke(
        credential: RespectPasswordCredential
    ) : AuthenticatePasswordUseCase.Response {
        val personEntity = schoolDb.getPersonEntityDao().findByUsername(credential.username)
            ?: throw IllegalArgumentException()

        val expectedPassword = schoolDb.getPersonPasswordEntityDao().findByUid(
            uidNumberMapper(personEntity.person.pGuid)
        ) ?: throw ForbiddenException("Invalid username/password")

        val credentialEncrypted = encryptPersonPasswordUseCase(
            EncryptPersonPasswordUseCase.Request(
                personGuid = personEntity.person.pGuid,
                password = credential.password,
                salt = expectedPassword.authSalt,
            )
        )

        if (
            !credentialEncrypted.authEncoded.decodeBase64Bytes().contentEquals(
                expectedPassword.authEncoded.decodeBase64Bytes()
            )
        ) {
            throw ForbiddenException("Invalid username/password")
        }

        return AuthenticatePasswordUseCase.Response(
            authenticatedPerson = personEntity.toPersonEntities().toModel()
        )
    }
}