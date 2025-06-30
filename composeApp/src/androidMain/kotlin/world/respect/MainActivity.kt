package world.respect

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import world.respect.app.app.App
import world.respect.view.app.AbstractAppActivity

class MainActivity : AbstractAppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}