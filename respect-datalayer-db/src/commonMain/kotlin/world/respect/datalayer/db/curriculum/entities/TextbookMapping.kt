package world.respect.datalayer.db.curriculum.entities

data class TextbookMapping(
    val uid: Long = 0L,
    val title: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
)