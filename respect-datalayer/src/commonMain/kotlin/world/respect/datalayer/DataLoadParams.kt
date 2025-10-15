package world.respect.datalayer

data class DataLoadParams(
    val mustRevalidate: Boolean = false,
    val onlyIfCached: Boolean = false,
)
