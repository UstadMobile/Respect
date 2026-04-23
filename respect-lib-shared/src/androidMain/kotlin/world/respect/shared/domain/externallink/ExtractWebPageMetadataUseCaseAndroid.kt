package world.respect.shared.domain.externallink

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ustadmobile.libcache.webview.OkHttpWebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume

class ExtractWebPageMetadataUseCaseAndroid(
    private val context: Context,
    private val json: Json,
) : ExtractWebPageMetadataUseCase {

    class BlockHeavyResourcesFilter : OkHttpWebViewClient.ShouldInterceptRequestFilter {
        override fun shouldIntercept(request: WebResourceRequest): Boolean {
            if (!request.isForMainFrame) {
                return true
            }
            return false
        }
    }

    private val blockHeavyResourcesFilter: OkHttpWebViewClient.ShouldInterceptRequestFilter = 
        BlockHeavyResourcesFilter()

    @SuppressLint("SetJavaScriptEnabled")
    override suspend fun invoke(url: String): WebPageMetadata = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            var webView: WebView? = null
            var isResumed = false
            var timeoutHandler: Handler? = null

            fun cleanupWebView() {
                try {
                    webView?.stopLoading()
                    webView?.destroy()
                } catch (_: Exception) {

                }
            }

            fun resumeWithMetadata(metadata: WebPageMetadata) {
                if (!isResumed) {
                    isResumed = true
                    timeoutHandler?.removeCallbacksAndMessages(null)
                    continuation.resume(metadata)

                    cleanupWebView()
                }
            }

            try {
                webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = false
                    settings.blockNetworkImage = true
                    settings.blockNetworkLoads = false
                    settings.mediaPlaybackRequiresUserGesture = true
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                    settings.databaseEnabled = false
                    settings.saveFormData = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                }

                timeoutHandler = Handler(Looper.getMainLooper()).apply {
                    postDelayed({
                        resumeWithMetadata(WebPageMetadata())
                    }, WEBVIEW_TIMEOUT_MS)
                }

                var capturedTitle: String? = null

                webView.webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        capturedTitle = title?.takeIf { it.isNotBlank() }
                    }
                }

                webView.webViewClient = object : WebViewClient() {

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        if (request != null && blockHeavyResourcesFilter.shouldIntercept(request)) {
                            return WebResourceResponse(
                                "text/plain",
                                "utf-8",
                                ByteArrayInputStream(byteArrayOf())
                            )
                        }
                        return null
                    }

                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)
                        val js = """
                            (function() {
                                var description = '';
                                var image = '';
                                
                                var descMeta = document.querySelector('meta[name="description"]');
                                if (descMeta) {
                                    description = descMeta.getAttribute('content') || '';
                                }
                                
                                var ogImage = document.querySelector('meta[property="og:image"]');
                                if (ogImage) {
                                    image = ogImage.getAttribute('content') || '';
                                } else {
                                    var imgMeta = document.querySelector('meta[name="image"]');
                                    if (imgMeta) {
                                        image = imgMeta.getAttribute('content') || '';
                                    }
                                }
                                
                                return JSON.stringify({description: description, image: image});
                            })();
                        """.trimIndent()

                        view?.evaluateJavascript(js) { result ->
                            try {
                                val jsonString = result?.trim('"')?.replace("\\\"", "\"") ?: "{}"
                                val jsResult = json.decodeFromString<JsMetadataResult>(jsonString)

                                val resolvedImageUrl = jsResult.image.takeIf { it.isNotBlank() }?.let {
                                    resolveUrl(it, url)
                                }

                                val metadata = WebPageMetadata(
                                    title = capturedTitle,
                                    description = jsResult.description.takeIf { it.isNotBlank() },
                                    imageUrl = resolvedImageUrl
                                )

                                if (!isResumed) {
                                    isResumed = true
                                    timeoutHandler?.removeCallbacksAndMessages(null)
                                    continuation.resume(metadata)
                                    cleanupWebView()
                                }
                            } catch (_: Exception) {
                                if (!isResumed) {
                                    isResumed = true
                                    timeoutHandler?.removeCallbacksAndMessages(null)
                                    continuation.resume(WebPageMetadata(title = capturedTitle))
                                    cleanupWebView()
                                }
                            }
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: android.webkit.WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            resumeWithMetadata(WebPageMetadata())
                        }
                    }

                    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        resumeWithMetadata(WebPageMetadata())
                    }

                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: android.webkit.RenderProcessGoneDetail?
                    ): Boolean {
                        resumeWithMetadata(WebPageMetadata())
                        return true
                    }
                }

                continuation.invokeOnCancellation {
                    timeoutHandler?.removeCallbacksAndMessages(null)
                    cleanupWebView()
                }

                webView.loadUrl(url)

            } catch (_: Exception) {
                timeoutHandler?.removeCallbacksAndMessages(null)
                cleanupWebView()
                resumeWithMetadata(WebPageMetadata())
            }
        }
    }

    private fun resolveUrl(imageUrl: String, baseUrl: String): String {
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl
        }

        val protocolEnd = baseUrl.indexOf("://")
        if (protocolEnd == -1) return imageUrl

        val pathStart = baseUrl.indexOf("/", protocolEnd + 3)
        if (pathStart == -1) {
            return "$baseUrl/$imageUrl"
        }

        val lastSlash = baseUrl.lastIndexOf("/")
        val basePath = baseUrl.substring(0, lastSlash + 1)

        return basePath + imageUrl
    }

    @Serializable
    private data class JsMetadataResult(
        val description: String = "",
        val image: String = ""
    )

    companion object {
        private const val WEBVIEW_TIMEOUT_MS = 10_000L
    }
}