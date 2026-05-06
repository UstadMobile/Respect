package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable

/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 *
 * Note: the member list may be returned in any order as per the xAPI spec, so we can reorder it to
 * produce a consistent hash
 *
 * @property member as per the xAPI spec the member property is optional for identified groups.
 */
@Serializable
data class XapiGroup(
    override val name: String? = null,
    override val mbox: String? = null,
    override val mbox_sha1sum: String? = null,
    override val openid: String? = null,
    override val objectType: XapiObjectType? = null,
    override val account: XapiAccount? = null,
    val member: List<XapiAgent>? = null,
): XapiActor, XapiStatementObject {

    /**
     * True if this is an anonymous group
     */
    val isAnonymous: Boolean
        get() = name == null && mbox == null && mbox_sha1sum == null &&
                openid == null && account == null

    val isIdentified: Boolean = !isAnonymous


    companion object {
        const val RESULT_KEY_GROUP_UPDATED = "groupUpdated"
        const val CLASS = "class/"
    }
}

