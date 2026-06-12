package world.respect.xapi.ipc.server

import android.app.Application
import androidx.room.Room
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.XapiResource
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm

class IpcTestApplication: Application(), XapiResourceProvider{

    internal val schoolDatabase: RespectSchoolDatabase by lazy {
        Room.databaseBuilder<RespectSchoolDatabase>(
            this, ""
        ).build()
    }

    internal val userUidStr = "1"

    internal val json = Json { encodeDefaults = false }

    internal val schoolUrl = Url("http://localhost/")

    internal val authUser = AuthenticatedUserPrincipalId(
        userUidStr
    )

    internal val numMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm())

    internal val schoolDataSource: SchoolDataSource by lazy {
        SchoolDataSourceDb(
            schoolDb = schoolDatabase,
            uidNumberMapper = numMapper,
            authenticatedUser = authUser,
            checkPersonPermissionUseCase = CheckPersonPermissionUseCaseDbImpl(
                authenticatedUser = authUser,
                schoolDb = schoolDatabase,
                uidNumberMapper = numMapper,
            ),
            json = json,
            defaultAppCatalogUrl = "http://localhost/not-used-here-buddy",
            schoolUrl = schoolUrl,
        )
    }


    override fun provideXapiResource(
        endpoint: String,
        authentication: String?
    ): XapiResource {
        TODO()
    }

}