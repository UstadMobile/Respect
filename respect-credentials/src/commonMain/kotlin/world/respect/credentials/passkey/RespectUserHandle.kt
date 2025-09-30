package world.respect.credentials.passkey

import io.ktor.http.Url

/**
 * As per the specs the user handle is specified by the relying party (e.g. RESPECT app) as an
 * identifier for a specific user account. It is an opaque byte sequence.
 *
 * The passkey userHandle (e.g. PublicKeyCredentialUserEntityJSON.id) will be Base64 encoded byte
 * array containing:
 *
 *  a) The PersonPasskey.personPasskeyUid (64bit long UID) - the UID of the PersonPasskey as stored
 *     in the database. (NOTE: This should actually change to the personUidNum because the user
 *     handle is supposed to be unique to a given account).
 *  b) The School URL (encoded using string.toByteArray())
 *
 * As per https://w3c.github.io/webauthn/#user-handle and
 * https://w3c.github.io/webauthn/#dictionary-user-credential-params
 * the userHandle MUST NOT contain any personally identifiable information (like usernames, email,
 * phone etc).
 *
 * This user handle allows the app to verify the passkey on the server because it includes a) the
 * learning space url and b) the uid of the passkey itself. The server can then authenticate by
 * decoding the user handle, getting the PersonPasskey uid, and then looking it up in the database
 * as per VerifySignInWithPasskeyUseCase.
 */
data class RespectUserHandle(
    val personPasskeyUid: Long,
    val schoolUrl: Url,
)