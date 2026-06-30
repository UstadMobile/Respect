package world.respect.app.domain.testing

import android.content.Context
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.domain.school.SchoolDbPath
import world.respect.shared.domain.testing.DbFileForUpload
import world.respect.shared.domain.testing.GetDbFilesForUploadUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

class GetDbFilesForUploadUseCaseAndroid(
    private val context: Context,
) : GetDbFilesForUploadUseCase, KoinComponent {

    override suspend fun invoke(schoolUrl: Url): DbFileForUpload? {
        val schoolDbName = SchoolDbPath.forSchoolUrl(schoolUrl).filename
        val dbFile = context.getDatabasePath(schoolDbName)
        if (!dbFile.exists()) return null

        val db = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(schoolUrl, null).scopeId
        ).get<RespectSchoolDatabase>()

        return withContext(Dispatchers.IO) {
            db.openHelper.writableDatabase.query(PRAGMA_CHECKPOINT).close()
            db.close()

            DbFileForUpload(filename = schoolDbName, bytes = dbFile.readBytes())
        }
    }
    companion object {

        private const val PRAGMA_CHECKPOINT = "PRAGMA wal_checkpoint(FULL)"


    }
}
