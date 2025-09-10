package world.respect.datalayer.http.ext

import io.ktor.http.Url
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.libutil.ext.resolve

suspend fun SchoolDirectoryDataSource.resolveRespectExtUrlForSchool(
    schoolUrl: Url,
    href: String,
): Url {
    val schoolDirectoryData = getSchoolDirectoryEntryByUrl(schoolUrl)
    val schoolDirectory = schoolDirectoryData.dataOrNull()
    println("school dir = $schoolDirectory")

    return schoolDirectory?.respectExt?.resolve(href)
        ?: throw IllegalStateException("SchoolUrl $schoolUrl has no respect extensions URL")
}
