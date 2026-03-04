package world.respect.datalayer.school.model

data class Bookmark(
    val personUid: String,
    val learningUnitManifestUrl:String,
    val title: String?,
    val subtitle: String?,
    val appIcon: String,
    val appName: String,
    val iconUrl: String?,
    val appManifestUrl: String,
    val expectedIdentifier: String,
    val refererUrl: String,
)