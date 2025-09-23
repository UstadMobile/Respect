package world.respect.credentials.passkey.request

import io.ktor.http.Url
import io.ktor.util.encodeBase64
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import world.respect.credentials.passkey.model.AuthenticatorSelectionCriteria
import world.respect.credentials.passkey.model.PublicKeyCredentialCreationOptionsJSON
import world.respect.credentials.passkey.model.PublicKeyCredentialParameters
import world.respect.credentials.passkey.model.PublicKeyCredentialRpEntity
import world.respect.credentials.passkey.model.PublicKeyCredentialUserEntityJSON
import world.respect.datalayer.db.opds.entities.PersonPasskeyEntity
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.ext.randomString

/**
 * Create the Json that is used to request creation of a new passkey. This should work on all
 * platforms where passkeys are supported (Android, Web, etc).
 *
 * As per https://w3c.github.io/webauthn/#dictdef-publickeycredentialcreationoptionsjson
 *
 * The passkey rpId will always be the SystemUrlConfig.systemBaseUrl host
 * The passkey user.id will be a unique 64bit long.
 *
 * The userHandle (PublicKeyCredentialUserEntityJSON.id) is encoded to include the person passkey
 * UID and Learning Space URL - see EncodeUserHandleUseCase
 */
class CreatePublicKeyCredentialCreationOptionsJsonUseCase(
    private val encodeUserHandleUseCase: EncodeUserHandleUseCase,
    private val appName: StringResource,
    private val primaryKeyGenerator: PrimaryKeyGenerator
) {

    suspend operator fun invoke(
        username: String,
        rpId: String,
    ): PublicKeyCredentialCreationOptionsJSON {
        val challenge = randomString(16) //TODO note: this should really take place on the server side

        val personPasskeyUid = primaryKeyGenerator.nextId(PersonPasskeyEntity.TABLE_ID)
        val encodeUserHandle = encodeUserHandleUseCase(personPasskeyUid)

        return PublicKeyCredentialCreationOptionsJSON(
            rp = PublicKeyCredentialRpEntity(
                id = rpId,
                name = getString(appName),
                icon = null,
            ),
            user = PublicKeyCredentialUserEntityJSON(
                id = encodeUserHandle,
                name = username,
                displayName = username,
            ),
            //Important: timeout may be optional as per the spec, but if omitted, Google Password
            //Manager won't work as expected
            timeout = PublicKeyCredentialCreationOptionsJSON.TIME_OUT_VALUE,
            challenge = challenge.encodeBase64(),
            pubKeyCredParams = listOf(
                PublicKeyCredentialParameters(
                    type = PublicKeyCredentialParameters.TYPE_PUBLIC_KEY,
                    alg = PublicKeyCredentialParameters.ALGORITHM_ES256
                ),
                PublicKeyCredentialParameters(
                    type = PublicKeyCredentialParameters.TYPE_PUBLIC_KEY,
                    alg = PublicKeyCredentialParameters.ALGORITHM_RS256
                ),
            ),
            authenticatorSelection = AuthenticatorSelectionCriteria(
                authenticatorAttachment = "platform",
                residentKey = "required"
            )
        )
    }

}