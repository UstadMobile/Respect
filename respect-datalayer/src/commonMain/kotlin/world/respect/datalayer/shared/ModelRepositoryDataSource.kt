package world.respect.datalayer.shared

interface ModelRepositoryDataSource<T: Any> {

    val local: LocalModelDataSource<T>

    val remote: WritableDataSource<T>

}