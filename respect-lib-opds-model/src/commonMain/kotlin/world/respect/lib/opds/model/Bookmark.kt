package world.respect.lib.opds.model

data class Bookmark(
    val title: String?,
    val subtitle: String?,
    val appIcon:String,
    val appName:String,
    val iconUrl: String?,
    val isBookmarked: Boolean
)