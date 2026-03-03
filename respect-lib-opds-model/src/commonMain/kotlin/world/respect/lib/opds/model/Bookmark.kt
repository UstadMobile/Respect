package world.respect.lib.opds.model

import com.eygraber.uri.Url

data class Bookmark(
    val learningUnitUrl:String,
    val title: String?,
    val subtitle: String?,
    val appIcon: String,
    val appName: String,
    val iconUrl: String?,
    val appManifestUrl: String,
    val expectedIdentifier: String,
    val refererUrl: String
)