package world.respect.lib.dataloadstate

data class DataLoadParams(
    val mustRevalidate: Boolean = false,
    val onlyIfCached: Boolean = false,
)
