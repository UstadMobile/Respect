@file:Suppress("UnusedImport")

package world.respect

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.app.datasource.SingleDataSourceProvider
import world.respect.app.datasource.fakeds.FakeRespectAppDataSourceProvider
import world.respect.app.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.app.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.app.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.app.viewmodel.apps.list.AppListViewModel
import world.respect.app.viewmodel.assignments.AssignmentViewModel
import world.respect.app.viewmodel.clazz.ClazzViewModel
import world.respect.app.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import world.respect.app.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.app.viewmodel.report.ReportViewModel
import world.respect.datasource.db.RespectAppDataSourceDb
import world.respect.datasource.db.RespectDatabase
import world.respect.datasource.http.RespectAppDataSourceHttp
import world.respect.datasource.repository.RespectAppDataSourceRepository
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm


@Suppress("unused")
const val DEFAULT_COMPATIBLE_APP_LIST_URL = "https://respect.world/respect-ds/manifestlist.json"

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
        OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                }
            ).build()
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

    viewModelOf(::AppsDetailViewModel)
    viewModelOf(::AppLauncherViewModel)
    viewModelOf(::EnterLinkViewModel)
    viewModelOf(::AppListViewModel)
    viewModelOf(::AssignmentViewModel)
    viewModelOf(::ClazzViewModel)
    viewModelOf(::LearningUnitListViewModel)
    viewModelOf(::LearningUnitDetailViewModel)
    viewModelOf(::ReportViewModel)


    // Uncomment this to switch to using fake data source provider for development purposes
//     single<RespectAppDataSourceProvider> {
//         FakeRespectAppDataSourceProvider()
//    }
     //*/

    //Uncomment to switch to using real datasource

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
                ),
                remote = RespectAppDataSourceHttp(
                    httpClient = get(),
                    defaultCompatibleAppListUrl = DEFAULT_COMPATIBLE_APP_LIST_URL,
                )
            )
        )
    }
}
