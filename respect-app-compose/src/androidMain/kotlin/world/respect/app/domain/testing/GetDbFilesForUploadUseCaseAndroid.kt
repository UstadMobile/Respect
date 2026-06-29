package world.respect.app.domain.testing

import android.content.Context
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.shared.domain.testing.DbFileForUpload
import world.respect.shared.domain.testing.GetDbFilesForUploadUseCase
import java.io.File

class GetDbFilesForUploadUseCaseAndroid(
    private val context: Context,
) : GetDbFilesForUploadUseCase {

    override suspend fun invoke(schoolUrl: Url): DbFileForUpload? {
        val schoolDbName = "school_3_${schoolUrl.sanitizedForFilename()}"
        val dbFile = context.getDatabasePath(schoolDbName)
        if (!dbFile.exists()) return null

        val backupFile = withContext(Dispatchers.IO) {
            File.createTempFile("db_backup_", ".db", context.cacheDir)
        }

        dbFile.copyTo(backupFile, overwrite = true)
        return DbFileForUpload(filename = schoolDbName, bytes = backupFile.readBytes())
    }


}
