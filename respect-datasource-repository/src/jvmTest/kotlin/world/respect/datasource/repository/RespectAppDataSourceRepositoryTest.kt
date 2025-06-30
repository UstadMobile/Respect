package world.respect.datasource.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import world.respect.datasource.DataLoadParams
import world.respect.datasource.http.compatibleapps.CompatibleAppDataSourceHttp
import world.respect.datasource.repository.compatibleapps.CompatibleAppDataSourceRepository
import world.respect.datasource.sqldelight.RespectDb
import world.respect.datasource.sqldelight.compatibleapps.CompatibleAppsDataSourceSqld
import java.util.Properties

@Suppress("unused")
class RespectAppDataSourceRepositoryTest {

    //This test is for rough prototyping and design experimentation only.
    //@Test
    fun runIt() {
        val driver: SqlDriver = JdbcSqliteDriver(
            JdbcSqliteDriver.IN_MEMORY, Properties(), RespectDb.Schema
        )
        //RespectDb.Schema.create(driver)
        val respectDb = RespectDb(driver)

        val json = Json { ignoreUnknownKeys = true }

        // http://localhost/opds/respect-ds/manifestlist.json

        val okHttpClient = OkHttpClient.Builder().build()

        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = json)
            }
            engine {
                preconfigured = okHttpClient
            }
        }

        val localDataSource = CompatibleAppsDataSourceSqld(respectDb.compatibleAppEntityQueries, json)
        val httpDataSource = CompatibleAppDataSourceHttp(
            httpClient,
            "http://localhost/opds/respect-ds/manifestlist.json"
        )
        val repository = CompatibleAppDataSourceRepository(localDataSource, httpDataSource)

        runBlocking {
            repository.getAddableApps(DataLoadParams()).collect { state ->
                println(state)
            }
        }
    }


}