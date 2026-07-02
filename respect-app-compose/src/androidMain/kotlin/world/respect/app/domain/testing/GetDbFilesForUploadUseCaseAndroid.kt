package world.respect.app.domain.testing

import android.content.Context
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.school.SchoolDbPath
import world.respect.shared.domain.testing.DbFileForUpload
import world.respect.shared.domain.testing.GetDbFilesForUploadUseCase

class GetDbFilesForUploadUseCaseAndroid(
    private val context: Context,
) : GetDbFilesForUploadUseCase {

    override suspend fun invoke(schoolUrl: Url): DbFileForUpload? {
        val schoolDbPath = SchoolDbPath.forSchoolUrl(schoolUrl)
        val dbFile = context.getDatabasePath(schoolDbPath.filename)
        if (!dbFile.exists()) return null
        return withContext(Dispatchers.IO) {
            DbFileForUpload(filename = schoolDbPath.filename, bytes = dbFile.readBytes())
        }
    }
}
