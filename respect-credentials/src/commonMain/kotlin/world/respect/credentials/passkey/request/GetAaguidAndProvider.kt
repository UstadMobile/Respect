package world.respect.credentials.passkey.request

import world.respect.credentials.passkey.model.PasskeyProviderInfo

/**
 * Use case to extract the Aaguid and provider name from passkey authenticator data.
 *
 * The Aaguid  is a unique identifier assigned to each model of FIDO2/WebAuthn authenticator device.
 *
 * This use case takes the authenticator data received during passkey creation and returns
 * a [PasskeyProviderInfo] containing the extracted Aaguid and the provider name.
 *
 * to map the Aaguid with provider We are usingthe community-driven repository
 * https://github.com/passkeydeveloper/passkey-authenticator-aaguids

 * @return A [PasskeyProviderInfo] containing the Aaguid and provider name
 */
interface GetAaguidAndProvider {

    /**
     * @param authenticatorData The base64-encoded authenticator data received during passkey creation.
     *
     */
    suspend operator fun invoke(authenticatorData: String): PasskeyProviderInfo

}