package world.respect.app.datasource.fakeds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.compatibleapps.CompatibleAppsDataSource
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.opds.model.LangMapStringValue
import com.eygraber.uri.Uri
import world.respect.datasource.DataLoadState

class FakeAppDataSource : CompatibleAppsDataSource {

    override fun getApp(
        manifestUrl: String,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<RespectAppManifest>> = flow {

        val dummyAppManifest = RespectAppManifest(
            name = LangMapStringValue("Learning App"),
            description = LangMapStringValue("A sample RESPECT-compatible learning app."),
            license = "proprietary",
            website = Url("https://example.com"),
            icon = Url("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png"),
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
            )
        )
    }



    override fun getAddableApps(loadParams: DataLoadParams): Flow<DataLoadState<List<DataLoadResult<RespectAppManifest>>>> =
        flow {
            emit(
                DataLoadResult(
                    data = getDummyAppsList().map { DataLoadResult(it) },
                )
            )
        }

    override fun getLaunchpadApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<RespectAppManifest>>> = flow {

        emit(
            DataLoadResult(
                data = getDummyAppsList(),
            )
        )
    }

    override suspend fun addAppToLaunchpad(manifestUrl: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeAppFromLaunchpad(manifestUrl: String) {
        TODO("Not yet implemented")
    }

    private fun getDummyAppsList(): List<RespectAppManifest> {
        val dummyAppsList = listOf(
            RespectAppManifest(
                name = LangMapStringValue("Learning App"),
                description = LangMapStringValue("A sample RESPECT-compatible learning app."),
                license = "proprietary",
                website = Url("https://example.com"),
                icon = Url("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png"),
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
                description = LangMapStringValue("A sample RESPECT-compatible learning app."),
                license = "proprietary",
                website = Url("https://example.com"),
                icon = Url("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png"),
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
        return dummyAppsList
    }
}