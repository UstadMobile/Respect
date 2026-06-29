package world.respect.app.domain.testing

import android.content.Context
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.school.SchoolDbPath
import world.respect.shared.domain.testing.DbFileForUpload
import world.respect.shared.domain.testing.GetDbFilesForUploadUseCase
import java.io.File

class GetDbFilesForUploadUseCaseAndroid(
    private val context: Context,
) : GetDbFilesForUploadUseCase {

    override suspend fun invoke(schoolUrl: Url): DbFileForUpload? {
        val schoolDbName = SchoolDbPath.forSchoolUrl(schoolUrl).filename
        val dbFile = context.getDatabasePath(schoolDbName)
        if (!dbFile.exists()) return null

        val backupFile = withContext(Dispatchers.IO) {
            File.createTempFile(BACKUP_FILE_PREFIX, BACKUP_FILE_SUFFIX, context.cacheDir)
        }

        dbFile.copyTo(backupFile, overwrite = true)
        return DbFileForUpload(filename = schoolDbName, bytes = backupFile.readBytes())
    }

    companion object {

        private const val BACKUP_FILE_PREFIX = "db_backup_"

        private const val BACKUP_FILE_SUFFIX = ".db"

    }

}
