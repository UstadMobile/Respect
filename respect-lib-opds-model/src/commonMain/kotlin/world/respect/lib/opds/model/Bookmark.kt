package world.respect.lib.opds.model

data class Bookmark(
    val url: Long,
    val title: String?,
    val subtitle: String?,
    val appIcon:String,
    val appName:String,
    val iconUrl: String?
)