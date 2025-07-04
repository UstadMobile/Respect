package world.respect.app.app

import kotlinx.serialization.Serializable

sealed interface AppDestination

@Serializable
object AppLauncher : AppDestination

@Serializable
object Assignment : AppDestination

@Serializable
object Clazz : AppDestination

@Serializable
object Report : AppDestination

@Serializable
object AppList : AppDestination

@Serializable
object EnterLink : AppDestination

/**
 * @property manifestUrl the URL to the RespectAppManifest for the given Respect compatible app
 */
@Serializable
data class AppsDetail(
    val manifestUrl: String
) : AppDestination

/**
 * @property opdsFeedUrl the URL for an OPDS feed containing a list of learning units and/or links
 *           to other feeds
 */
@Serializable
data class LearningUnitList(
    val opdsFeedUrl: String
) : AppDestination


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
data class LearningUnitDetail(
    val learningUnitManifestUrl: String,
    val refererUrl: String? = null,
    val expectedIdentifier: String? = null
) : AppDestination

