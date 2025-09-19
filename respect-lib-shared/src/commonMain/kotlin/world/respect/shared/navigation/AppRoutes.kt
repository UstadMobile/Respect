//Transient properties are used as documented below, cannot be removed because they are needed for serialization
@file:Suppress("CanBeParameter")

package world.respect.shared.navigation

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.report.ReportFilter
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

/**
 * Mostly TypeSafe navigation for the RESPECT app. All serialized properties must be primitives or
 * strings (8/July/25: Compose multiplatform navigation does not like custom types when used with
 * toRoute).
 *
 * If using a non-primitive type (e.g. Url) then use a private constructor property with a primitive
 * type and then add a transient property
 */

@Serializable
sealed interface RespectAppRoute

@Serializable
object Acknowledgement : RespectAppRoute

@Serializable
object JoinClazzWithCode : RespectAppRoute

@Serializable
data class LoginScreen (
    val schoolUrlStr: String,
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)


    companion object {
        fun create(realmUrl: Url) = LoginScreen(realmUrl.toString())
    }

}

@Serializable
object RespectAppLauncher : RespectAppRoute

@Serializable
object Assignment : RespectAppRoute

@Serializable
object ClazzList : RespectAppRoute

@Serializable
class ClazzDetail(
    val guid: String,
) : RespectAppRoute

@Serializable
class AddPersonToClazz(
    val roleTypeStr: String,
    val inviteCode: String? = null,
) : RespectAppRoute {

    @Transient
    val roleType = EnrollmentRoleEnum.fromValue(roleTypeStr)

    companion object {
        fun create(
            roleType: EnrollmentRoleEnum,
            inviteCode: String?,
        ) = AddPersonToClazz(
            roleTypeStr = roleType.value,
            inviteCode = inviteCode,
        )
    }
}


@Serializable
data class ClazzEdit(
    val guid: String?
) : RespectAppRoute

@Serializable
object Report : RespectAppRoute

@Serializable
class ReportEdit(val reportUid: String?) : RespectAppRoute

@Serializable
class ReportDetail(val reportUid: String) : RespectAppRoute

@Serializable
class ReportEditFilter(
    private val reportFilterJson: String
) : RespectAppRoute {

    @Transient
    val reportFilter: ReportFilter =
        Json.decodeFromString(ReportFilter.serializer(), reportFilterJson)

    companion object {
        fun create(reportFilter: ReportFilter): ReportEditFilter {
            val jsonStr = Json.encodeToString(ReportFilter.serializer(), reportFilter)
            return ReportEditFilter(jsonStr)
        }
    }
}

@Serializable
object ReportTemplateList : RespectAppRoute

@Serializable
object IndicatorList : RespectAppRoute

@Serializable
class IndicatorDetail(val indicatorUid: String) : RespectAppRoute

@Serializable
class IndictorEdit(val indicatorId: String?) : RespectAppRoute

@Serializable
object RespectAppList : RespectAppRoute

@Serializable
object EnterLink : RespectAppRoute

@Serializable
object GetStartedScreen : RespectAppRoute

@Serializable
object OtherOption : RespectAppRoute

@Serializable
object HowPasskeyWorks : RespectAppRoute

/**
 * @property manifestUrl the URL to the RespectAppManifest for the given Respect compatible app
 */
@Serializable
class AppsDetail private constructor(
    private val manifestUrlStr: String
) : RespectAppRoute {

    @Transient
    val manifestUrl = Url(manifestUrlStr)

    companion object {

        fun create(manifestUrl: Url): AppsDetail {
            return AppsDetail(manifestUrl.toString())
        }

    }
}


/**
 * @property opdsFeedUrl the URL for an OPDS feed containing a list of learning units and/or links
 *           to other feeds
 */
@Serializable
class LearningUnitList(
    private val opdsFeedUrlStr: String,
    private val appManifestUrlStr: String,
) : RespectAppRoute {

    @Transient
    val opdsFeedUrl = Url(opdsFeedUrlStr)

    @Transient
    val appManifestUrl = Url(appManifestUrlStr)

    companion object {

        fun create(
            opdsFeedUrl: Url,
            appManifestUrl: Url,
        ): LearningUnitList {
            return LearningUnitList(
                opdsFeedUrl.toString(), appManifestUrl.toString()
            )
        }

    }

}

@Serializable
class EnterPasswordSignup private constructor(
    private val usernameStr: String,
    private val profileType: ProfileType,
      private val inviteCode: String,
    private val personInfoJson: String,

    ) : RespectAppRoute {

    @Transient
    val username = usernameStr
    @Transient
    val personInfo: RespectRedeemInviteRequest.PersonInfo = Json.decodeFromString(personInfoJson)

    @Transient
    val type = profileType

    @Transient
    val code = inviteCode
    companion object {
        fun create(
            username: String,
            profileType: ProfileType,
            inviteCode: String,
            personInfo: RespectRedeemInviteRequest.PersonInfo?

        ): EnterPasswordSignup {
            val personInfoJson = Json.encodeToString(personInfo)

            return EnterPasswordSignup(username,profileType, inviteCode,personInfoJson)
        }

    }
}

@Serializable
class OtherOptionsSignup private constructor(
    private val usernameStr: String,
    private val profileType: ProfileType,
    private val inviteCode: String,
    private val personInfoJson: String,
    ) : RespectAppRoute {

    @Transient
    val username = usernameStr

    @Transient
    val personInfo: RespectRedeemInviteRequest.PersonInfo = Json.decodeFromString(personInfoJson)

    @Transient
    val type = profileType

    @Transient
    val code = inviteCode
    companion object {

        fun create(
            username: String,
            profileType: ProfileType,
            inviteCode: String,
            personInfo: RespectRedeemInviteRequest.PersonInfo?

        ): OtherOptionsSignup {
            val personInfoJson = Json.encodeToString(personInfo)

            return OtherOptionsSignup(username,profileType, inviteCode,personInfoJson)
        }

    }
}

@Serializable
class ConfirmationScreen(
    private val inviteCode: String
) : RespectAppRoute {

    @Transient
    val code = inviteCode

    companion object {
        fun create(inviteCode: String) = ConfirmationScreen(inviteCode)
    }
}

@Serializable
class WaitingForApproval(
    private val profileType: ProfileType,
    private val inviteCode: String,
    private val pendingInviteStateUid: String,

    ) : RespectAppRoute {

    @Transient
    val type = profileType

    @Transient
    val uid = pendingInviteStateUid

    @Transient
    val code = inviteCode

    companion object {
        fun create(profileType: ProfileType, inviteCode: String,pendingInviteStateUid:String): WaitingForApproval {
            return WaitingForApproval(profileType, inviteCode,pendingInviteStateUid)
        }
    }
}

@Serializable
class SignupScreen(
    private val profileType: ProfileType,
    private val inviteCode: String,
    private val username: String?,
    private val parentPersonInfoJson: String?,
    private val parentRedeemCredential: String?,
) : RespectAppRoute {

    @Transient
    val type = profileType

    @Transient
    val parentOrGuardianPersonInfo: RespectRedeemInviteRequest.PersonInfo? =
        if (parentPersonInfoJson!=null)
            Json.decodeFromString(parentPersonInfoJson)
        else null

    @Transient
    val parentOrGuardianRedeemCredential: RespectRedeemInviteRequest.RedeemInviteCredential? =
        if (parentRedeemCredential!=null)
            Json.decodeFromString(parentRedeemCredential)
        else null

    @Transient
    val parentUsername = username

    @Transient
    val code = inviteCode

    companion object {
        fun create(
            profileType: ProfileType,
            inviteCode: String,
            parentUsername: String? = null,
            parentPersonInfoJson: RespectRedeemInviteRequest.PersonInfo?=null,
            parentRedeemCredential: RespectRedeemInviteRequest.RedeemInviteCredential?=null
        ): SignupScreen {
            val personInfoJson = Json.encodeToString(parentPersonInfoJson)
            val parentRedeemCredential = Json.encodeToString(parentRedeemCredential)

            return SignupScreen(profileType, inviteCode, parentUsername,personInfoJson,parentRedeemCredential)
        }
    }
}

@Serializable
class TermsAndCondition(
    private val profileType: ProfileType,
      private val inviteCode: String,
) : RespectAppRoute {

    @Transient
    val type = profileType

    @Transient
    val code = inviteCode

    companion object {
        fun create(profileType: ProfileType, inviteCode: String): TermsAndCondition {
            return TermsAndCondition(profileType, inviteCode)
        }
    }
}

@Serializable
class CreateAccount(
    private val profileType: ProfileType,
      private val inviteCode: String,
    private val personInfoJson: String,
) : RespectAppRoute {

    @Transient
    val type = profileType
    @Transient
    val personInfo: RespectRedeemInviteRequest.PersonInfo = Json.decodeFromString(personInfoJson)

    @Transient
    val code = inviteCode

    companion object {
        fun create(
            profileType: ProfileType,
            inviteCode: String,
            personInfo: RespectRedeemInviteRequest.PersonInfo?
        ): CreateAccount {
            val personInfoJson = Json.encodeToString(personInfo)
            return CreateAccount(profileType, inviteCode,personInfoJson)
        }
    }
}

/**
 * @property learningUnitManifestUrl the URL of the OPDS Publication (Readium Manifest) for the
 *           learning unit as per RESPECT integration guide:
 *           https://github.com/UstadMobile/RESPECT-Consumer-App-Integration-Guide?tab=readme-ov-file#5-support-listing-and-launching-learning-units
 * @property refererUrl (optional), where available, the URL of the OPDS feed that referred the
 *           user to this learning unit. This allows the use of cached information from the feed
 *           to avoid waiting for the learningUnitManifestUrl to load to show the user the title,
 *           description, etc.
 * @property expectedIdentifier (optional), where a refererUrl is provided, to use cached feed
 *           metadata as above, the identifier of the publication within the feed.
 */
@Serializable
class LearningUnitDetail(
    private val learningUnitManifestUrlStr: String,
    private val appManifestUrlStr: String,
    private val refererUrlStr: String? = null,
    val expectedIdentifier: String? = null
) : RespectAppRoute {

    @Transient
    val learningUnitManifestUrl = Url(learningUnitManifestUrlStr)

    @Transient
    val refererUrl = refererUrlStr?.let { Url(it) }

    @Transient
    val appManifestUrl = Url(appManifestUrlStr)

    companion object {

        fun create(
            learningUnitManifestUrl: Url,
            appManifestUrl: Url,
            refererUrl: Url? = null,
            expectedIdentifier: String? = null
        ) = LearningUnitDetail(
            learningUnitManifestUrlStr = learningUnitManifestUrl.toString(),
            appManifestUrlStr = appManifestUrl.toString(),
            refererUrlStr = refererUrl?.toString(),
            expectedIdentifier = expectedIdentifier,
        )

    }

}

@Serializable
class LearningUnitViewer(
    private val learningUnitIdStr: String,
) : RespectAppRoute {

    @Transient
    val learningUnitId = Url(learningUnitIdStr)

    companion object {
        fun create(learningUnitId: Url): LearningUnitViewer {
            return LearningUnitViewer(
                learningUnitIdStr = learningUnitId.toString()
            )
        }
    }

}

@Serializable
object AccountList : RespectAppRoute


@Serializable
object PersonList : RespectAppRoute

@Serializable
data class PersonDetail(
    val guid: String,
) : RespectAppRoute

@Serializable
data class PersonEdit(
    val guid: String?,
) : RespectAppRoute
