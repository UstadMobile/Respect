package world.respect.app.domain.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.libuicompose.theme.RespectAppTheme
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import world.respect.app.view.testing.SendDbToServerScreen
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.shared.domain.testing.SendDbToServerUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.viewmodel.testing.SendDbToServerUiState

class SendDbToServerActivity : AppCompatActivity() {

    private val uiState = MutableStateFlow(SendDbToServerUiState())

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
                    val state by uiState.collectAsState()
                    SendDbToServerScreen(state)
                }
            }
        }

        lifecycleScope.launch {
            try {
                val scopeId = SchoolDirectoryEntryScopeId(schoolUrl, null).scopeId
                val schoolScope = getKoin().getScopeOrNull(scopeId)
                if (schoolScope != null) {
                    val db = schoolScope.get<RespectSchoolDatabase>()
                    db.openHelper.writableDatabase.execSQL(PRAGMA_WAL_CHECKPOINT)
                    schoolScope.close()
                }

                getKoin().get<SendDbToServerUseCase>().invoke(schoolUrl, name)
                uiState.value = SendDbToServerUiState(isLoading = false)
            } catch (e: Exception) {
                uiState.value = SendDbToServerUiState(isLoading = false, errorMessage = e.message)
            }
        }
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
