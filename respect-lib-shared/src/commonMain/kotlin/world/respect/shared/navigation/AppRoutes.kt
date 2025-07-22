//Transient properties are used as documented below, cannot be removed because they are needed for serialization
@file:Suppress("CanBeParameter")

package world.respect.shared.navigation

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
object RespectAppLauncher : RespectAppRoute

@Serializable
object Assignment : RespectAppRoute

@Serializable
object Clazz : RespectAppRoute

@Serializable
object Report : RespectAppRoute

@Serializable
class ReportEdit private constructor(
    private val reportUidStr: String
) : RespectAppRoute {

    @Transient
    val reportUid = reportUidStr.toLong()

    companion object {
        fun create(reportUid: Long): ReportEdit {
            return ReportEdit(reportUid.toString())
        }
    }
}

@Serializable
class ReportDetail private constructor(
    private val reportUidStr: String
) : RespectAppRoute {

    @Transient
    val reportUid = reportUidStr.toLong()

    companion object {
        fun create(reportUid: Long): ReportDetail {
            return ReportDetail(reportUid.toString())
        }
    }
}

@Serializable
object RespectAppList : RespectAppRoute

@Serializable
object EnterLink : RespectAppRoute


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
): RespectAppRoute {

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

