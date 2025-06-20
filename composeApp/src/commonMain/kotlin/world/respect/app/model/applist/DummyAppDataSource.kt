package world.respect.app.model.applist

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.DataResult
import world.respect.datasource.LoadingStatus
import world.respect.datasource.compatibleapps.CompatibleAppsDataSource
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.opds.model.LangMapStringValue
import com.eygraber.uri.Uri

class FakeAppDataSource: CompatibleAppsDataSource {

    override fun getApp(
        manifestUrl: String,
        loadParams: DataLoadParams
    ): Flow<DataResult<RespectAppManifest>> = flow {

        val dummyAppManifest = RespectAppManifest(
            name = LangMapStringValue("Learning App"),
            description =LangMapStringValue("A sample RESPECT-compatible learning app."),
            license = "proprietary",
            website = Url("https://example.com"),
            icon = Url("https://example.com/icon.png"),
            learningUnits = Uri.parse("https://example.com/opds/learning-units"),
            defaultLaunchUri = Uri.parse("https://example.com/launch"),
            android = RespectAppManifest.AndroidDetails(
                packageId = "com.example.dummylearningapp",
                stores = listOf(
                    Url("https://play.google.com/store/apps/details?id=com.example.dummylearningapp")
                ),
                sourceCode = Url("https://github.com/example/dummylearningapp")
            ),
            web = RespectAppManifest.WebDetails(
                url = Url("https://example.com/webapp"),
                sourceCode = Url("https://github.com/example/dummylearningapp-web")
            )
        )

        emit(
            DataLoadResult(
                data = dummyAppManifest,
                status = LoadingStatus.LOADED
            )
        )
    }

    override fun getAddableApps(loadParams: DataLoadParams): Flow<DataResult<RespectAppManifest>> {
        TODO("Not yet implemented")
    }

    override fun getLaunchpadApps(
        loadParams: DataLoadParams
    ): Flow<DataResult<List<RespectAppManifest>>> = flow {

        val dummyApps = listOf(
            RespectAppManifest(
                name = LangMapStringValue("Learning App"),
                description =LangMapStringValue("A sample RESPECT-compatible learning app."),
                license = "proprietary",
                website = Url("https://example.com"),
                icon = Url("https://example.com/icon.png"),
                learningUnits = Uri.parse("https://example.com/opds/learning-units"),
                defaultLaunchUri = Uri.parse("https://example.com/launch"),
                android = RespectAppManifest.AndroidDetails(
                    packageId = "com.example.dummylearningapp",
                    stores = listOf(
                        Url("https://play.google.com/store/apps/details?id=com.example.dummylearningapp")
                    ),
                    sourceCode = Url("https://github.com/example/dummylearningapp")
                ),
                web = RespectAppManifest.WebDetails(
                    url = Url("https://example.com/webapp"),
                    sourceCode = Url("https://github.com/example/dummylearningapp-web")
                )
            ),
            RespectAppManifest(
                name = LangMapStringValue("Chimple"),
                description =LangMapStringValue("A sample RESPECT-compatible learning app."),
                license = "proprietary",
                website = Url("https://example.com"),
                icon = Url("https://example.com/icon.png"),
                learningUnits = Uri.parse("https://example.com/opds/learning-units"),
                defaultLaunchUri = Uri.parse("https://example.com/launch"),
                android = RespectAppManifest.AndroidDetails(
                    packageId = "com.example.dummylearningapp",
                    stores = listOf(
                        Url("https://play.google.com/store/apps/details?id=com.example.dummylearningapp")
                    ),
                    sourceCode = Url("https://github.com/example/dummylearningapp")
                ),
                web = RespectAppManifest.WebDetails(
                    url = Url("https://example.com/webapp"),
                    sourceCode = Url("https://github.com/example/dummylearningapp-web")
                )
            )

        )

        emit(
            DataLoadResult(
                data = dummyApps,
                status = LoadingStatus.LOADED
            )
        )
    }

    override suspend fun addAppToLaunchpad(manifestUrl: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeAppFromLaunchpad(manifestUrl: String) {
        TODO("Not yet implemented")
    }

}