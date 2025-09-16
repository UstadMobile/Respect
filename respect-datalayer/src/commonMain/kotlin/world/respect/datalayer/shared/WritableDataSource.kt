package world.respect.datalayer.shared

interface WritableDataSource<T: Any> {

    suspend fun store(list: List<T>)

}