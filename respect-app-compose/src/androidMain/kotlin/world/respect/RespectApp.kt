package world.respect

import android.app.Application
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import android.webkit.WebView
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import okhttp3.OkHttpClient
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RespectApp : Application(), SingletonImageLoader.Factory {

    //See https://stackoverflow.com/questions/23844667/how-do-i-detect-if-i-am-in-release-or-debug-mode
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())

        WebView.setWebContentsDebuggingEnabled(
            applicationInfo.flags.and(FLAG_DEBUGGABLE) == FLAG_DEBUGGABLE
        )

        startKoin {
            androidContext(this@RespectApp)
            modules(appKoinModule)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val okHttpClient: OkHttpClient = get()
        return ImageLoader.Builder(applicationContext)
            .components {
                //As per https://coil-kt.github.io/coil/network/#using-a-custom-okhttpclient
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient } ))
            }
            .crossfade(true)
            .build()
    }

}
