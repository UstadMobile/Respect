package world.respect.datalayer.http.ext

import io.ktor.http.Url
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.libutil.ext.appendEndpointSegments

suspend fun SchoolDirectoryEntryDataSource.resolveRespectExtUrlForSchool(
    schoolUrl: Url,
    resourcePath: String,
): Url {
    val schoolDirectoryData = getSchoolDirectoryEntryByUrl(schoolUrl)
    val schoolDirectory = schoolDirectoryData.dataOrNull()

    return schoolDirectory?.respectExt?.appendEndpointSegments(resourcePath)
        ?: throw IllegalStateException("SchoolUrl $schoolUrl has no respect extensions URL")
}
