package world.respect.datalayer.shared

/**
 * Represents a local datasource for a specific model e.g. Person, Class, etc.
 */
interface LocalModelDataSource<T : Any> {

    /**
     * updateLocalFromRemote is used to handle when new data has been received from the remote
     * data source. Local data will normally only be replaced when it is newer than the local data
     * (e.g. to avoid overwriting local data that has not yet been sent).
     *
     * It is NOT subject to permission checks (this function is for data is being received from a
     * trusted server or peer).
     *
     * @param list - specific list of model data to insert
     * @param forceOverwrite normally local data will only be updated if it is newer than what is
     *        stored locally. Sometimes (e.g. during conflict resolution) local data may be
     *        overridden anyway.
     */
    suspend fun updateLocalFromRemote(
        list: List<T>,
        forceOverwrite: Boolean = false,
    )

    /**
     * findByUidList is used by the Remote Write Queue Drainer to get the data models to be sent.
     */
    suspend fun findByUidList(
        uids: List<String>,
    ): List<T>



}