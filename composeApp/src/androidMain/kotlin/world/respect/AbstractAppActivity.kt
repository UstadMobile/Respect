package world.respect.view.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.ustadmobile.libuicompose.theme.RespectAppTheme
import world.respect.app.app.SizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import world.respect.app.app.App


abstract class AbstractAppActivity : AppCompatActivity() {

    val WindowWidthSizeClass.multiplatformSizeClass: SizeClass
        get() = when (this) {
            WindowWidthSizeClass.Compact -> SizeClass.COMPACT
            WindowWidthSizeClass.Medium -> SizeClass.MEDIUM
            WindowWidthSizeClass.Expanded -> SizeClass.EXPANDED
            else -> SizeClass.MEDIUM
        }

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
        ExperimentalMaterial3WindowSizeClassApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

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
                    )

                }

            }
        }
    }

}
