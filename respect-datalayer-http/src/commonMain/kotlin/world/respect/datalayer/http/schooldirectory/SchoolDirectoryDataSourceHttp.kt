package world.respect.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource

class SchoolDirectoryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal
): SchoolDirectoryDataSource{
    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        TODO("Not yet implemented")
    }

}