package world.respect.view.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.compose.rememberNavController
import com.ustadmobile.libuicompose.theme.RespectAppTheme
import io.ktor.http.Url
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.android.ext.android.getKoin
import world.respect.app.app.App
import world.respect.app.app.SizeClass
import world.respect.shared.domain.createlink.CreateLinkUseCase.Companion.QUERY_PARAM
import world.respect.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectComposeNavController


abstract class AbstractAppActivity : AppCompatActivity() {

    private val customDeepLinkToUrlUseCase = getKoin().get<CustomDeepLinkToUrlUseCase>()

    val WindowWidthSizeClass.multiplatformSizeClass: SizeClass
        get() = when (this) {
            WindowWidthSizeClass.Compact -> SizeClass.COMPACT
            WindowWidthSizeClass.Medium -> SizeClass.MEDIUM
            WindowWidthSizeClass.Expanded -> SizeClass.EXPANDED
            else -> SizeClass.MEDIUM
        }

    private val _navCommandFlow = MutableSharedFlow<NavCommand>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navCommandFlow: SharedFlow<NavCommand> = _navCommandFlow.asSharedFlow()

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
        ExperimentalMaterial3WindowSizeClassApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            val windowSizeClass = calculateWindowSizeClass(this)

            val respectNavController = remember(navController) {
                RespectComposeNavController(navController)
            }


            intent.data?.let { uri ->
                val url = try {
                    Url(uri.toString())
                } catch (_: Exception) {
                    null
                }
                navigateToAcknowledgeScreen(url)
            }
            RespectAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            testTagsAsResourceId = true
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(
                        widthClass = windowSizeClass.widthSizeClass.multiplatformSizeClass,
                        navController = navController,
                        activityNavCommandFlow = navCommandFlow,
                        respectNavController = respectNavController
                    )

                }

            }
        }
    }
    fun navigateToAcknowledgeScreen(url: Url?){
        url?.let {
            val inviteCode = url.parameters[QUERY_PARAM]

            val navCommand = customDeepLinkToUrlUseCase(url)
            navCommand.let {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = Acknowledgement.create(
                            schoolUrl = url,
                            inviteCode = inviteCode
                        ),clearBackStack = false
                    )
                )
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            val url = try {
                Url(uri.toString())
            } catch (_: Exception) {
                null
            }
            navigateToAcknowledgeScreen(url)
        }
    }
}
