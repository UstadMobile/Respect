package world.respect

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.ustadmobile.libcache.webview.OkHttpWebViewClient
import org.koin.android.ext.android.inject
import world.respect.app.R
import world.respect.shared.domain.launchapp.LaunchAppUseCaseAndroid

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