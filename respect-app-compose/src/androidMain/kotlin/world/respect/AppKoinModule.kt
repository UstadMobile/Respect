@file:Suppress("UnusedImport")

package world.respect

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.db.ClearNeighborsCallback
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import com.ustadmobile.libcache.webview.OkHttpWebViewClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.respect.shared.datasource.RespectAppDataSourceProvider
import world.respect.shared.datasource.SingleDataSourceProvider
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.assignments.AssignmentViewModel
import world.respect.shared.viewmodel.clazz.ClazzViewModel
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.report.ReportViewModel
import world.respect.datalayer.db.RespectAppDataSourceDb
import world.respect.datalayer.db.RespectDatabase
import world.respect.datalayer.http.RespectAppDataSourceHttp
import world.respect.datalayer.repository.RespectAppDataSourceRepository
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.domain.launchapp.LaunchAppUseCaseAndroid
import world.respect.shared.domain.storage.CachePathsProviderAndroid
import world.respect.shared.domain.storage.GetAndroidSdCardDirUseCase
import world.respect.shared.domain.storage.GetOfflineStorageOptionsUseCaseAndroid
import world.respect.shared.domain.storage.GetOfflineStorageSettingUseCase
import java.io.File
import kotlinx.io.files.Path
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.domain.report.query.RunReportUseCaseClientImpl
import world.respect.shared.domain.report.query.RunReportUseCaseDatabaseImpl
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel
import world.respect.shared.viewmodel.report.list.ReportListViewModel


@Suppress("unused")
const val DEFAULT_COMPATIBLE_APP_LIST_URL = "https://respect.world/respect-ds/manifestlist.json"

const val SHARED_PREF_SETTINGS_NAME = "respect_settings"

val appKoinModule = module {
    single<Json> {
        Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        }
    }

    single<XXStringHasher> {
        XXStringHasherCommonJvm()
    }

    single<OkHttpClient> {
        val cachePathProvider: CachePathsProvider = get()

        OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                }
            )
            .addInterceptor(
                UstadCacheInterceptor(
                    cache = get(),
                    tmpDirProvider = { File(cachePathProvider().tmpWorkPath.toString()) },
                    logger = NapierLoggingAdapter(),
                    json = get(),
                )
            )
            .build()
    }

    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = get())
            }
            engine {
                preconfigured = get()
            }
        }
    }

    single<LaunchAppUseCase> {
        LaunchAppUseCaseAndroid(
            appContext = androidContext().applicationContext
        )
    }

    viewModelOf(::AppsDetailViewModel)
    viewModelOf(::AppLauncherViewModel)
    viewModelOf(::EnterLinkViewModel)
    viewModelOf(::AppListViewModel)
    viewModelOf(::AssignmentViewModel)
    viewModelOf(::ClazzViewModel)
    viewModelOf(::LearningUnitListViewModel)
    viewModelOf(::LearningUnitDetailViewModel)
    viewModelOf(::ReportViewModel)
    viewModelOf(::ReportDetailViewModel)
    viewModelOf(::ReportEditViewModel)
    viewModelOf(::ReportListViewModel)

    single<GetOfflineStorageOptionsUseCase> {
        GetOfflineStorageOptionsUseCaseAndroid(
            getAndroidSdCardDirUseCase = get()
        )
    }

    single<GetAndroidSdCardDirUseCase> {
        GetAndroidSdCardDirUseCase(
            appContext = androidContext().applicationContext
        )
    }

    single<GetOfflineStorageSettingUseCase> {
        GetOfflineStorageSettingUseCase(
            getOfflineStorageOptionsUseCase = get(),
            settings = get(),
        )
    }

    single<CachePathsProvider> {
        CachePathsProviderAndroid(
            appContext = androidContext().applicationContext,
            getAndroidSdCardPathUseCase = get(),
            getOfflineStorageSettingUseCase = get(),
        )
    }

    single<Settings> {
        SharedPreferencesSettings(
            delegate = androidContext().getSharedPreferences(
                SHARED_PREF_SETTINGS_NAME,
                Context.MODE_PRIVATE
            )
        )
    }

    single<UstadCacheDb> {
        Room.databaseBuilder(
            androidContext().applicationContext,
            UstadCacheDb::class.java,
            UstadCacheBuilder.DEFAULT_DB_NAME
        ).addCallback(ClearNeighborsCallback())
            .build()
    }

    single<UstadCache> {
        UstadCacheBuilder(
            appContext = androidContext().applicationContext,
            storagePath = Path(
                File(androidContext().filesDir, "httpfiles").absolutePath
            ),
            sizeLimit = { 100_000_000L },
            db = get(),
        ).build()
    }

    single<OkHttpWebViewClient> {
        OkHttpWebViewClient(
            okHttpClient = get()
        )
    }

    single<RespectAppDataSourceProvider> {
        val appContext = androidContext().applicationContext
        SingleDataSourceProvider(
            datasource = RespectAppDataSourceRepository(
                local = RespectAppDataSourceDb(
                    respectDatabase = Room.databaseBuilder<RespectDatabase>(
                        appContext, appContext.getDatabasePath("respect.db").absolutePath
                    ).setDriver(BundledSQLiteDriver())
                    .build(),
                    json = get(),
                    xxStringHasher = get(),
                    primaryKeyGenerator = PrimaryKeyGenerator(RespectDatabase.TABLE_IDS),
                ),
                remote = RespectAppDataSourceHttp(
                    httpClient = get(),
                    defaultCompatibleAppListUrl = DEFAULT_COMPATIBLE_APP_LIST_URL,
                )
            )
        )
    }
    single<RunReportUseCase> {
        RunReportUseCaseDatabaseImpl()
    }
    single<CreateGraphFormatterUseCase> {
        CreateGraphFormatterUseCase()
    }
}
