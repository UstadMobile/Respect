package world.respect.datasource

/**
 * Wrapper interface for data loading states; covers both remote and local database scenarios.
 *
 * @property metaInfo Loading status of the data
 */
sealed interface DataLoadState<T: Any> {
    val metaInfo: DataLoadMetaInfo
    val localMetaInfo: DataLoadMetaInfo?
    val remoteMetaInfo: DataLoadMetaInfo?

}

data class DataLoadingState<T: Any>(
    override val metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(status = LoadingStatus.LOADING),
    override val localMetaInfo: DataLoadMetaInfo? = null,
    override val remoteMetaInfo: DataLoadMetaInfo? = null,
): DataLoadState<T>

data class DataLoadResult<T: Any>(
    val data: T? = null,
    override val metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(status = LoadingStatus.LOADED),
    override val localMetaInfo: DataLoadMetaInfo? = null,
    override val remoteMetaInfo: DataLoadMetaInfo? = null,
): DataLoadState<T>

data class DataErrorResult<T: Any>(
    val error: Throwable,
    override val metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(status = LoadingStatus.FAILED),
    override val localMetaInfo: DataLoadMetaInfo? = null,
    override val remoteMetaInfo: DataLoadMetaInfo? = null,
): DataLoadState<T>

