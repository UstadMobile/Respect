package world.respect.datalayer.db.school.entities

data class ContentJobItemProgressAndStatus(
    var status: Int = 0,
    var progress: Long = 0,
    var total: Long = 0,
)