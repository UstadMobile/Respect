package world.respect.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.libutil.ext.appendEndpointPathSegments

class SchoolDirectoryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal
): SchoolDirectoryDataSource{
    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        val directory = local.schoolDirectoryDataSource.getDirectoryByInviteCode(inviteCode)
            ?: throw IllegalArgumentException("directory not found for invite code $inviteCode")

        val url = URLBuilder(directory.baseUrl).appendEndpointPathSegments(
            listOf("api/directory/invite")
        ).also {
            it.parameters["code"] = inviteCode
        }.build()

        return httpClient.get(url).body()
    }

    override suspend fun deleteDirectory(directory: RespectSchoolDirectory) {
        // Implement delete
    }

}