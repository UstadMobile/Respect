package world.respect.datalayer.db.curriculum.entities

data class TextbookMapping(
    var uid: Long = 0L,
    var title: String? = null,
    var description: String? = null,
    var coverImageUrl: String? = null,
    var createdDate: Long = System.currentTimeMillis(),
)