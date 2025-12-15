package world.respect.sharedse.domain.account.authenticatepassword

import io.ktor.http.Url
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.libutil.util.throwable.ForbiddenException
import world.respect.shared.domain.account.authenticatepassword.AuthenticateQrBadgeUseCase

class AuthenticateQrBadgeUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : AuthenticateQrBadgeUseCase {

    override suspend fun invoke(
        credential: RespectQRBadgeCredential
    ): AuthenticateQrBadgeUseCase.Response {

        // First, find the QR badge by the URL from the credential
        val qrCodeEntity = schoolDb.getPersonQrBadgeEntityDao().findByQrCodeUrl(credential.qrCodeUrl.toString())
            ?: throw ForbiddenException("QR badge not found")

        // Verify the URL matches exactly
        if (qrCodeEntity.pqrQrCodeUrl != credential.qrCodeUrl.toString()) {
            throw ForbiddenException("QR badge URL mismatch")
        }

        // Find the person using the GUID from the QR code entity
        val personEntity = schoolDb.getPersonEntityDao().findByGuidNum(
            uidNumberMapper(qrCodeEntity.pqrGuid)
        )?.toPersonEntities()?.toModel()
            ?: throw ForbiddenException("Person not found")

        return AuthenticateQrBadgeUseCase.Response(
            authenticatedPerson = personEntity
        )
    }
}