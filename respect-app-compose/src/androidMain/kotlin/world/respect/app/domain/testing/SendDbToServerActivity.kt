package world.respect.app.domain.testing

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.libuicompose.theme.RespectAppTheme
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.getKoin
import world.respect.app.view.testing.SendDbToServerScreen
import world.respect.app.view.testing.SendDbToServerUiState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.shared.domain.school.SchoolDbPath
import world.respect.shared.domain.testing.SendDbToServerUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

class SendDbToServerActivity : AppCompatActivity() {

    private var uiState by mutableStateOf(SendDbToServerUiState())

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val schoolUrlStr = intent.getStringExtra(EXTRA_SCHOOL_URL) ?: run { finish(); return }
        val name = intent.getStringExtra(EXTRA_NAME) ?: run { finish(); return }
        val schoolUrl = Url(schoolUrlStr)

        setContent {
            RespectAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true },
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SendDbToServerScreen(uiState)
                }
            }
        }

        lifecycleScope.launch {
            uiState = try {
                upload(schoolUrl, name)
                SendDbToServerUiState(isLoading = false)
            } catch (e: Exception) {
                SendDbToServerUiState(isLoading = false, errorMessage = e.message)
            }
        }
    }

    private suspend fun upload(schoolUrl: Url, name: String) {
        val scopeId = SchoolDirectoryEntryScopeId(schoolUrl, null).scopeId
        val scope = getKoin().getScopeOrNull(scopeId)
        if (scope != null) {
            scope.get<RespectSchoolDatabase>().openHelper.writableDatabase.execSQL(PRAGMA_WAL_CHECKPOINT)
            scope.close()
        } else {
            val dbFile = getDatabasePath(SchoolDbPath.forSchoolUrl(schoolUrl).filename)
            if (dbFile.exists()) {
                withContext(Dispatchers.IO) {
                    SQLiteDatabase.openDatabase(
                        dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE
                    ).use { db ->
                        db.rawQuery(PRAGMA_WAL_CHECKPOINT, null).use { it.moveToFirst() }
                    }
                }
            }
        }
        getKoin().get<SendDbToServerUseCase>().invoke(schoolUrl, name)
    }

    companion object {
        private const val EXTRA_SCHOOL_URL = "school_url"
        private const val EXTRA_NAME = "db_name"
        private const val PRAGMA_WAL_CHECKPOINT = "PRAGMA wal_checkpoint(FULL)"

        fun createIntent(context: Context, schoolUrlStr: String, name: String): Intent =
            Intent(context, SendDbToServerActivity::class.java)
                .putExtra(EXTRA_SCHOOL_URL, schoolUrlStr)
                .putExtra(EXTRA_NAME, name)
    }
}
