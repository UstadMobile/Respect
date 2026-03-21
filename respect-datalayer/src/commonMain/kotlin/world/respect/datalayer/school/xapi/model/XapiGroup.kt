package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.Serializable

/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 *
 * Note: the member list may be returned in any order as per the xAPI spec, so we can reorder it to
 * produce a consistent hash
 */
@Serializable
data class XapiGroup(
    override val name: String? = null,
    override val mbox: String? = null,
    override val mbox_sha1sum: String? = null,
    override val openid: String? = null,
    override val objectType: XapiObjectType? = null,
    override val account: XapiAccount? = null,
    val member: List<XapiAgent> = emptyList(),
): XapiActor, XapiStatementObject

val XapiGroup.isAnonymous: Boolean
    get() = mbox == null && openid == null && account == null
