package world.respect.shared.domain.externallink

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

class ExtractWebPageMetadataUseCaseAndroid(
    private val context: Context,
    private val json: Json,
) : ExtractWebPageMetadataUseCase {

    @SuppressLint("SetJavaScriptEnabled")
    override suspend fun invoke(url: String): WebPageMetadata = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            var webView: WebView? = null
            var isResumed = false

            try {
                webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = false
                    settings.blockNetworkImage = true
                    settings.blockNetworkLoads = false
                    settings.mediaPlaybackRequiresUserGesture = true
                }

                var capturedTitle: String? = null

                webView.webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        capturedTitle = title?.takeIf { it.isNotBlank() }
                    }
                }

                webView.webViewClient = object : WebViewClient() {

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
                                    continuation.resume(metadata)
                                }
                            } catch (_: Exception) {
                                if (!isResumed) {
                                    isResumed = true
                                    continuation.resume(WebPageMetadata(title = capturedTitle))
                                }
                            } finally {
                                webView?.destroy()
                            }
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
                        if (!isResumed) {
                            isResumed = true
                            continuation.resume(WebPageMetadata())
                            webView?.destroy()
                        }
                    }
                }

                continuation.invokeOnCancellation {
                    webView?.destroy()
                }

                webView.loadUrl(url)

            } catch (_: Exception) {
                webView?.destroy()
                if (!isResumed) {
                    continuation.resume(WebPageMetadata())
                }
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
}