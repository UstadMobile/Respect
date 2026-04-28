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
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import world.respect.libutil.ext.resolve
import java.io.ByteArrayInputStream

class ExtractWebPageMetadataUseCaseAndroid(
    private val context: Context,
    private val json: Json,
) : ExtractWebPageMetadataUseCase {


    @SuppressLint("SetJavaScriptEnabled")
    override suspend fun invoke(url: String): WebPageMetadata = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<WebPageMetadata>()
        var webView: WebView? = null
        var timeoutHandler: Handler? = null

        fun cleanupWebView() {
            try {
                webView?.stopLoading()
                webView?.destroy()
            } catch (e: Exception) {
                    /*
                     * WebView cleanup can throw exceptions if already destroyed or in invalid state.
                     * and failure here doesn't affect the metadata result already returned.
                     */
                Napier.w("WebView cleanup failed", e, tag = LOG_TAG)
            }
        }

        fun cleanup() {
            timeoutHandler?.removeCallbacksAndMessages(null)
            cleanupWebView()
        }

        fun completeWithMetadata(metadata: WebPageMetadata) {
            if (!deferred.isCompleted) {
                cleanup()
                deferred.complete(metadata)
            }
        }

        try {
                webView = WebView(context).apply {
                    // Enable JavaScript to execute evaluateJavascript() for extracting meta tags from DOM
                    // Reference: https://developer.android.com/reference/android/webkit/WebSettings#setJavaScriptEnabled(boolean)
                    settings.javaScriptEnabled = true
                    // Enable DOM storage API which may be required by some pages for JavaScript execution
                    // Reference: https://developer.android.com/reference/android/webkit/WebSettings#setDomStorageEnabled(boolean)
                    settings.domStorageEnabled = true
                    // Disable image loading to reduce memory and prevent crashes during metadata-only extraction
                    // Reference: https://developer.android.com/reference/android/webkit/WebSettings#setLoadsImagesAutomatically(boolean)
                    // Reference: https://developer.android.com/reference/android/webkit/WebSettings#setBlockNetworkImage(boolean)
                    settings.loadsImagesAutomatically = false
                    settings.blockNetworkImage = true
                    // Prevent media auto-play which could cause crashes during metadata extraction
                    // Reference: https://developer.android.com/reference/android/webkit/WebSettings#setMediaPlaybackRequiresUserGesture(boolean)
                    settings.mediaPlaybackRequiresUserGesture = true
                }

            timeoutHandler = Handler(Looper.getMainLooper()).apply {
                postDelayed({
                    completeWithMetadata(WebPageMetadata())
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
                        /* Block all non-main-frame resources during metadata extraction to prevent:
                         * - WebView renderer crashes from heavy video/audio resources
                         * - Excessive memory usage during metadata-only operations
                         * - Slow page loads when only HTML meta tags are needed
                         *
                         * Only allow main frame (HTML document) to load.
                         * Block all subresources: images, videos, audio, CSS, JS, fonts, etc.
                         *
                         * Reference: https://developer.android.com/reference/android/webkit/WebViewClient#shouldInterceptRequest(android.webkit.WebView,%20android.webkit.WebResourceRequest)
                         */
                        if (request != null && !request.isForMainFrame) {
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

                        // Check if webView is still valid before executing JavaScript
                        val currentWebView = webView
                        if (currentWebView == null) {
                            /* WebView was destroyed before page finished loading */
                            Napier.w("WebView is null in onPageFinished", tag = LOG_TAG)
                            completeWithMetadata(WebPageMetadata(title = capturedTitle))
                            return
                        }
                        /* Extract metadata from HTML by querying standard meta tags in the DOM.
                         *
                         * Web Standards Used:
                         * 1. document.querySelector() - Standard DOM API to find HTML elements
                         *    Reference: https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelector
                         * 2. getAttribute() - Standard method to read HTML attribute values
                         *    Reference: https://developer.mozilla.org/en-US/docs/Web/API/Element/getAttribute
                         * 3. JSON.stringify() - Safely return structured data from JavaScript to Kotlin
                         *    Reference: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/JSON/stringify
                         *
                         * Metadata extracted:
                         * - description: <meta name="description"> (HTML standard)
                         *   Reference: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta/name
                         * - image: <meta name="image"> (if available)
                         */
                        val js = """
                            (function() {
                                var description = '';
                                var image = '';

                                var descMeta = document.querySelector('meta[name="description"]');
                                if (descMeta) {
                                    description = descMeta.getAttribute('content') || '';
                                }

                                var imgMeta = document.querySelector('meta[name="image"]');
                                if (imgMeta) {
                                    image = imgMeta.getAttribute('content') || '';
                                }

                                return JSON.stringify({description: description, image: image});
                            })();
                        """.trimIndent()

                        // Execute JavaScript to extract metadata from DOM
                        // Reference: https://developer.android.com/reference/android/webkit/WebView#evaluateJavascript(java.lang.String,%20android.webkit.ValueCallback%3Cjava.lang.String%3E)
                        currentWebView.evaluateJavascript(js) { result ->
                            try {
                                val jsonString = result?.let { json.decodeFromString<String>(it) } ?: "{}"
                                val jsResult = json.decodeFromString<JsMetadataResult>(jsonString)
                                val resolvedImageUrl = jsResult.image.takeIf { it.isNotBlank() }?.let {
                                    Url(url).resolve(it).toString()
                                }


                                val metadata = WebPageMetadata(
                                title = capturedTitle,
                                description = jsResult.description.takeIf { it.isNotBlank() },
                                imageUrl = resolvedImageUrl
                            )
                            completeWithMetadata(metadata)
                            } catch (e: Exception) {
                                /* JSON parsing can fail if the page doesn't have proper meta tags or
                                 * if JavaScript execution fails. return partial metadata (title only).
                                 */
                                Napier.w("Failed to parse metadata from JavaScript result", e, tag = LOG_TAG)
                                completeWithMetadata(WebPageMetadata(title = capturedTitle))
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
                        completeWithMetadata(WebPageMetadata())
                    }
                }
                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: android.webkit.RenderProcessGoneDetail?
                ): Boolean {
                    completeWithMetadata(WebPageMetadata())
                    return true
                }
            }
            deferred.invokeOnCompletion { cleanup() }
            webView.loadUrl(url)

            deferred.await()
        } catch (e: Exception) {
                  /* 
                    * WebView creation or setup can fail (e.g., WebView not available on device,
                    * security restrictions, etc.). log and return empty metadata.
                    */
            Napier.w("Failed to create or load WebView", e, tag = LOG_TAG)
            completeWithMetadata(WebPageMetadata())
            deferred.await()
        }
    }
    @Serializable
    private data class JsMetadataResult(
        val description: String = "",
        val image: String = ""
    )

    companion object {
        private const val WEBVIEW_TIMEOUT_MS = 10_000L
        private const val LOG_TAG = "ExtractWebPageMetadata"
    }
}

