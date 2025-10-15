package world.respect.datalayer.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.compatibleapps.CompatibleAppDataSourceDb
import world.respect.datalayer.http.compatibleapps.CompatibleAppDataSourceHttp
import world.respect.datalayer.repository.compatibleapps.CompatibleAppDataSourceRepository
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm

@Suppress("unused")
class RespectAppDataSourceRepositoryTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    //This test should only be enabled for testing/architecture experimentation
    //@Test
    fun runIt() {
        val dbFile = temporaryFolder.newFile("respect.db")
        val db = Room.databaseBuilder<RespectAppDatabase>(dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()

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

        val xxStringHasher = XXStringHasherCommonJvm()
        val localDataSource = CompatibleAppDataSourceDb(db, json, xxStringHasher)
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