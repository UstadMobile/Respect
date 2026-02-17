//Transient properties are used as documented below, cannot be removed because they are needed for serialization
@file:Suppress("CanBeParameter")

package world.respect.shared.navigation

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.report.ReportFilter
import world.respect.shared.ext.NextAfterScan
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import world.respect.shared.viewmodel.manageuser.signup.SignupScreenModeEnum
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryMode

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
data class Acknowledgement (
    val schoolUrlStr: String?=null,
    val inviteCode: String? = null
    ) : RespectAppRoute {

    @Transient
    val schoolUrl =  schoolUrlStr?.let { Url(it) }

    companion object {
        fun create(schoolUrl: Url? = null,inviteCode: String?=null) =
            Acknowledgement(schoolUrl.toString(),inviteCode)
    }

}
@Serializable
data class EnterInviteCode(
    val schoolUrlStr: String
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(schoolUrl: Url) = EnterInviteCode(schoolUrl.toString())
    }

}

@Serializable
object Onboarding : RespectAppRoute

@Serializable
data class SchoolDirectoryList(
    val modeStr: String = SchoolDirectoryMode.MANAGE.value
) : RespectAppRoute {

    @Transient
    val mode: SchoolDirectoryMode = SchoolDirectoryMode.fromValue(modeStr)

    companion object {
        fun create(
            mode: SchoolDirectoryMode = SchoolDirectoryMode.MANAGE
        ) = SchoolDirectoryList(mode.value)
    }

}

@Serializable
object SchoolDirectoryEdit : RespectAppRoute

@Serializable
data class LoginScreen(
    val schoolUrlStr: String,
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(schoolUrl: Url) = LoginScreen(schoolUrl.toString())
    }

}

@Serializable
data class RespectAppLauncher(
    val resultDestStr: String? = null,
) : RespectAppRoute, RouteWithResultDest{

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    companion object {
        fun create(
            resultDest: ResultDest? = null,
        ) = RespectAppLauncher(
            resultDestStr = resultDest.encodeToJsonStringOrNull()
        )
    }
}

@Serializable
object AssignmentList : RespectAppRoute

@Serializable
data class AssignmentDetail(
    val uid: String,
) : RespectAppRoute

@Serializable
data class AssignmentEdit(
    val guid: String?,
    private val learningUnitStr: String? = null,
): RespectAppRoute {

    @Transient
    val learningUnitSelected: LearningUnitSelection? = learningUnitStr?.let {
        Json.decodeFromString(LearningUnitSelection.serializer(), it)
    }

    companion object {

        fun create(
            uid: String?,
            learningUnitSelected: LearningUnitSelection? = null,
        ) = AssignmentEdit(
            guid = uid,
            learningUnitStr = learningUnitSelected?.let {
                Json.encodeToString(LearningUnitSelection.serializer(), it)
            },
        )

    }

}

@Serializable
object ClazzList : RespectAppRoute

@Serializable
class ClazzDetail(
    val guid: String,
) : RespectAppRoute

@Serializable
data class EnrollmentList(
    val filterByPersonUid: String,
    val roleStr: String,
    val filterByClassUid: String
) : RespectAppRoute {

    @Transient
    val role = EnrollmentRoleEnum.fromValue(roleStr)

    companion object  {
        fun create(
            filterByPersonUid: String,
            role: EnrollmentRoleEnum,
            filterByClassUid: String
        ) : EnrollmentList {
            return EnrollmentList(
                filterByPersonUid = filterByPersonUid,
                roleStr = role.value,
                filterByClassUid = filterByClassUid
            )
        }
    }

}

@Serializable
data class EnrollmentEdit(
    val uid: String?,
    val role: String,
    val personGuid: String,
    val clazzGuid: String,
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
data class GetStartedScreen(
    val canGoBack: Boolean = false,
) : RespectAppRoute

@Serializable
object OtherOption : RespectAppRoute

@Serializable
object HowPasskeyWorks : RespectAppRoute

/**
 * @property manifestUrl the URL to the RespectAppManifest for the given Respect compatible app
 */
@Serializable
class AppsDetail private constructor(
    private val manifestUrlStr: String,
    private val resultDestStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    val manifestUrl = Url(manifestUrlStr)

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    companion object {

        fun create(
            manifestUrl: Url,
            resultDest: ResultDest? = null,
        ): AppsDetail {
            return AppsDetail(
                manifestUrlStr = manifestUrl.toString(),
                resultDestStr = resultDest?.encodeToJsonStringOrNull()
            )
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
    private val resultDestStr: String?,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    val opdsFeedUrl = Url(opdsFeedUrlStr)

    @Transient
    val appManifestUrl = Url(appManifestUrlStr)

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    companion object {

        fun create(
            opdsFeedUrl: Url,
            appManifestUrl: Url,
            resultDest: ResultDest? = null,
        ): LearningUnitList {
            return LearningUnitList(
                opdsFeedUrlStr = opdsFeedUrl.toString(),
                appManifestUrlStr = appManifestUrl.toString(),
                resultDestStr = resultDest.encodeToJsonStringOrNull()
            )
        }

    }

}
@Serializable
class EnterPasswordSignup private constructor(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest : RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest,
        ): EnterPasswordSignup {
            return EnterPasswordSignup(
                schoolUrl.toString(),
                Json.encodeToString(inviteRequest)
            )
        }

    }
}

@Serializable
class OtherOptionsSignup private constructor(
    private val inviteRedeemRequestStr: String,
    private val schoolUrlStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest : RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {

        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest,
        ): OtherOptionsSignup {
            val respectRedeemInviteRequest = Json.encodeToString(inviteRequest)

            return OtherOptionsSignup(
                respectRedeemInviteRequest, schoolUrl.toString()
            )
        }

    }
}

@Serializable
class AcceptInvite(
    val schoolUrlStr: String,
    val code: String,
    val canGoBack: Boolean = true,
    val isTeacherOrAdmin: Boolean = false,
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            code: String,
            canGoBack: Boolean = true,
            isTeacherOrAdmin: Boolean = false
        ) = AcceptInvite(
            schoolUrlStr = schoolUrl.toString(),
            code = code,
            canGoBack = canGoBack,
            isTeacherOrAdmin = isTeacherOrAdmin
        )
    }
}

@Serializable
class WaitingForApproval : RespectAppRoute

@Serializable
class SignupScreen(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
    private val signupModeStr: String,
    private val parentPersonStr: String?,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest : RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val signupMode: SignupScreenModeEnum = SignupScreenModeEnum.fromValue(signupModeStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    @Transient
    val parentPerson: Person? = parentPersonStr?.let { Json.decodeFromString(it) }

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest,
            signupMode: SignupScreenModeEnum = SignupScreenModeEnum.STANDARD,
            parentPerson: Person? = null,
        ): SignupScreen {
            return SignupScreen(
                schoolUrlStr = schoolUrl.toString(),
                inviteRedeemRequestStr = Json.encodeToString(inviteRequest),
                signupModeStr = signupMode.value,
                parentPersonStr = parentPerson?.let { Json.encodeToString(it) }
            )
        }
    }
}

@Serializable
class TermsAndCondition(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest : RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest
        ): TermsAndCondition {
            return TermsAndCondition(
                schoolUrlStr = schoolUrl.toString(),
                inviteRedeemRequestStr = Json.encodeToString(inviteRequest)
            )
        }
    }
}

@Serializable
data class SchoolRegistrationComplete(
    val schoolUrl: String = "",
    val authToken: String? = null
) : RespectAppRoute

@Serializable
class CreateAccount(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest : RespectRedeemInviteRequest = Json.decodeFromString(
        inviteRedeemRequestStr
    )

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest
        ): CreateAccount {
            return CreateAccount(
                schoolUrlStr = schoolUrl.toString(),
                inviteRedeemRequestStr = Json.encodeToString(inviteRequest)
            )
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


/**
 * @property addToClassUid if the PersonList screen has been navigated when the user clicks
 *           add student or add teacher on the ClassDetail screen, then the classUid.
 * @property addToClassRoleStr if the PersonList screen has been navigated when the user clicks
 *  *           add student or add teacher on the ClassDetail screen, then the role
 */
@Serializable
data class PersonList(
    private val filterByRoleStr: String? = null,
    val isTopLevel: Boolean = false,
    private val resultDestStr: String? = null,
    val inviteUid: String? = null,
    val classNameStr: String? = null,
    val addToClassUid: String? = null,
    val addToClassRoleStr: String? = null,
    val personGuidStr: String? = null,
    val hideInvite: Boolean = false,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    val filterByRole: PersonRoleEnum? = filterByRoleStr?.let {
        PersonRoleEnum.fromValue(it)
    }
    @Transient
    val role: EnrollmentRoleEnum? = addToClassRoleStr?.let {
        EnrollmentRoleEnum.fromValue(it)
    }
    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    companion object {

        fun create(
            filterByRole: PersonRoleEnum? = null,
            isTopLevel: Boolean = false,
            resultDest: ResultDest? = null,
            inviteUid: String? = null,
            className: String? = null,
            classUid: String? = null,
            personGuid: String? = null,
            role: EnrollmentRoleEnum? = null,
            hideInvite: Boolean = false,
        ) = PersonList(
            filterByRoleStr = filterByRole?.value,
            isTopLevel = isTopLevel,
            resultDestStr = resultDest.encodeToJsonStringOrNull(),
            inviteUid = inviteUid,
            addToClassUid = classUid,
            classNameStr = className,
            addToClassRoleStr = role?.value,
            personGuidStr = personGuid,
            hideInvite = hideInvite,
        )

    }
}

@Serializable
data class PersonDetail(
    val guid: String,
) : RespectAppRoute

@Serializable
data class PasskeyList(
    val guid: String,
) : RespectAppRoute


/**
 * @param guid the Uid of the Person account to manage as person Person.guid
 * @param setPersonQrBadgeUrlStr see setPersonQrBadgeUrl
 * @param setPersonQrBadgeUsername When setPersonQrBadgeUrl is non-null, this is the username that
 *        should be assigned to the person as per guid.
 */
@Serializable
data class ManageAccount(
    val guid: String,
    val setPersonQrBadgeUsername: String? = null,
    private val setPersonQrBadgeUrlStr: String? = null,
) : RespectAppRoute {

    /**
     * When a QR badge is first assigned as part of creating an account, this is the URL for the
     * badge. When the user flow is PersonDetail, CreateAccountSetUsername, ScanQRCode, ManageAccount.
     * ScanQRCode is not scoped to a particular school and cannot handle saving the QR code badge.
     */
    @Transient
    val setPersonQrBadgeUrl: Url? = setPersonQrBadgeUrlStr?.let { Url(it) }


    companion object {
        fun create(
            guid: String,
            qrUrl: Url? = null,
            username: String? = null,
        ) = ManageAccount(
            guid = guid,
            setPersonQrBadgeUrlStr = qrUrl?.toString(),
            setPersonQrBadgeUsername = username,
        )
    }
}

@Serializable
data class PersonEdit(
    val guid: String?,
    private val resultDestStr: String? = null,
    private val presetRoleStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val presetRole: PersonRoleEnum? = presetRoleStr?.let {
        PersonRoleEnum.fromValue(it)
    }

    companion object {
        fun create(
            guid: String?,
            resultDest: ResultDest? = null,
            presetRole: PersonRoleEnum? = null,
        ) = PersonEdit(
            guid = guid,
            resultDestStr = resultDest.encodeToJsonStringOrNull(),
            presetRoleStr = presetRole?.value,
        )
    }


}

@Serializable
data object Settings : RespectAppRoute

@Serializable
data class ScanQRCode(
    val guid: String? = null,
    val resultDestStr: String? = null,
    private val schoolUrlStr: String? = null,
    val username: String? = null,
    private val nextAfterScanStr: String? = null
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val schoolUrl: Url? = schoolUrlStr?.let { Url(it) }

    @Transient
    val nextAfterScan: NextAfterScan? = nextAfterScanStr?.let {
        NextAfterScan.valueOf(it)
    }

    companion object {
        fun create(
            guid: String? = null,
            resultDest: ResultDest? = null,
            schoolUrl: Url? = null,
            username: String? = null,
            nextAfterScan: NextAfterScan? = null
        ) = ScanQRCode(
            guid = guid,
            resultDestStr = resultDest?.encodeToJsonStringOrNull(),
            username = username,
            schoolUrlStr = schoolUrl?.toString(),
            nextAfterScanStr = nextAfterScan?.name
        )
    }
}

@Serializable
data object CurriculumMappingList : RespectAppRoute

@Serializable
data object SchoolSettings : RespectAppRoute

@Serializable
data object SharedDevicesSettings : RespectAppRoute

@Serializable
data object SelectClass : RespectAppRoute

@Serializable
data object TeacherAndAdminLogin : RespectAppRoute

@Serializable
data class StudentList(
    val className: String,
    val guid: String,
): RespectAppRoute

@Serializable
data class CurriculumMappingEdit(
    val textbookUid: Long = 0L,
    private val mappingDataJson: String? = null
) : RespectAppRoute {

    @Transient
    val mappingData: CurriculumMapping? = mappingDataJson?.let { jsonString ->
        try {
            Json.decodeFromString(CurriculumMapping.serializer(), jsonString)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun create(
            uid: Long,
            mappingData: CurriculumMapping? = null
        ) = CurriculumMappingEdit(
            textbookUid = uid,
            mappingDataJson = mappingData?.let { mapping ->
                try {
                    Json.encodeToString(CurriculumMapping.serializer(), mapping)
                } catch (e: Exception) {
                    null
                }
            }
        )
    }
}
@Serializable
data class CreateAccountSetUsername(
    val guid: String
): RespectAppRoute

@Serializable
data class CreateAccountSetPassword(
    val guid: String,
    val username: String? = null,
) : RespectAppRoute



@Serializable
data class ChangePassword(
    val guid: String,
): RespectAppRoute

@Serializable
data class InvitePerson(
    val invitePersonOptionsStr: String,
) : RespectAppRoute {

    /**
     * As there are three types of invitations, so there are three different types of invite options
     */
    @Serializable
    sealed interface InvitePersonOptions

    /**
     * @property if presetRole is set - then dropdown will not be displayed.
     */
    @Serializable
    @SerialName("newuser")
    data class NewUserInviteOptions(
        val presetRole: PersonRoleEnum?
    ): InvitePersonOptions

    @Serializable
    @SerialName("class")
    data class ClassInviteOptions(
        val inviteUid: String,
    ): InvitePersonOptions


    @Transient
    val invitePersonOptions: InvitePersonOptions = Json.decodeFromString(
        invitePersonOptionsStr
    )

    companion object {

        fun create(
            invitePersonOptions: InvitePersonOptions
        ) = InvitePerson(
            invitePersonOptionsStr = Json.encodeToString(invitePersonOptions)
        )
    }
}

@Serializable
data class QrCode(
    val inviteLink:String?=null,
    val schoolOrClass:String?=null
): RespectAppRoute

@Serializable
data class CopyCode(
    val inviteCode:String?=null
): RespectAppRoute
