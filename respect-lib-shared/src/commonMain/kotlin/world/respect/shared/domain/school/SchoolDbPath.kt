package world.respect.shared.domain.school

import io.ktor.http.Url
import world.respect.libutil.ext.sanitizedForFilename

data class SchoolDbPath(
    val filename: String,
) {
    companion object {

        private const val PREFIX = "school_3_"

        fun forSchoolUrl(schoolUrl: Url): SchoolDbPath =
            SchoolDbPath(PREFIX + schoolUrl.sanitizedForFilename())

    }
}
