package world.respect

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.ustadmobile.libcache.webview.OkHttpWebViewClient
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import world.respect.app.R
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.xapi.model.XapiVerb
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchapp.LaunchAppUseCaseAndroid
import world.respect.shared.domain.xapi.createLearningUnitStatement

/**
 * A separate activity that only shows a WebView (e.g. to view a LearningUnit) .
 *
 * This can't be done as normal Jetpack Compose using the AndroidView as normal because the vh css
 * unit doesn't work; content that uses 100vh etc comes out as zero height or a small percentage of
 * the screen (at random).
 */
class WebViewActivity : AppCompatActivity() {

    private val webChromeClient = object: WebChromeClient() {

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            this@WebViewActivity.title = title ?: ""
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            val progressBar = findViewById<ProgressBar>(R.id.progress_bar) ?: return

            if(newProgress < 100) {
                progressBar.progress = newProgress
                progressBar.takeIf { it.visibility != View.VISIBLE }?.visibility = View.VISIBLE
            }else {
                progressBar.takeIf { it.visibility != View.GONE }?.visibility = View.GONE
            }
        }
    }

    private val webViewClient: OkHttpWebViewClient by inject()
    private val accountManager: RespectAccountManager by inject()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        setSupportActionBar(findViewById(R.id.toolbar))
        val webView: WebView = findViewById(R.id.web_view)
        webView.webChromeClient = webChromeClient
        webView.webViewClient = webViewClient
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        //Content will be loaded from HTTPs and will then make requests to 127.0.0.1 for xAPI
        //statement submission
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        val url = intent.getStringExtra(LaunchAppUseCaseAndroid.EXTRA_URL) ?:
            throw IllegalStateException("No url specified")

        webView.loadUrl(url)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        val webView: WebView = findViewById(R.id.web_view)
        if(webView.canGoBack()) {
            webView.goBack()
            return true
        }else {
            finish()
            return true
        }
    }

    override fun finish() {
        val activityId =
            intent.getStringExtra(LaunchAppUseCaseAndroid.EXTRA_ACTIVITY_ID)

        if (activityId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    val account = accountManager.activeAccount ?: return@launch

                    val accountScope = accountManager.getOrCreateAccountScope(account)

                    val schoolDataSource: SchoolDataSource = accountScope.get()

                    val actor = accountManager.selectedAccountAndPersonFlow
                        .firstOrNull()
                        ?.xapiAgent
                        ?: return@launch

//                    schoolDataSource.xapiResource.statements.post(
//                        listOf(
//                            createLearningUnitStatement(
//                                activityId = activityId,
//                                actor = actor,
//                                verbId = XapiVerb.ID_TERMINATED,
//                            )
//                        )
//                    )
                }.onFailure {
                    Napier.e("Failed to send terminated statement", it)
                }
            }
        }

        super.finish()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_webview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.webview_close -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}