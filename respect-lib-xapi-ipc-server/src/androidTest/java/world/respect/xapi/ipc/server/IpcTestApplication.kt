package world.respect.xapi.ipc.server

import android.app.Application
import androidx.room.Room
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import world.respect.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.XapiResource
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm

class IpcTestApplication: Application(), XapiResourceProvider{

    internal val schoolDatabase: RespectSchoolDatabase by lazy {
        Room.databaseBuilder<RespectSchoolDatabase>(
            this, "school_db"
        ).build()
    }

    internal val adminUserUid = "1"

    internal val json = Json { encodeDefaults = false }

    internal val schoolUrl = Url("http://localhost/")

    internal val authUser = AuthenticatedUserPrincipalId(adminUserUid)

    internal val numMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm())

    internal val adminPerson = Person(
        guid = adminUserUid,
        givenName = "Admin",
        familyName = "User",
        gender = PersonGenderEnum.UNSPECIFIED,
        roles = listOf(
            PersonRole(true, PersonRoleEnum.SYSTEM_ADMINISTRATOR)
        ),
    )

    internal val schoolDataSource: SchoolDataSourceLocal by lazy {
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

    internal var useDefaultPermissions = true

    suspend fun insertAdminAndDefaultGrants() {
        schoolDataSource.personDataSource.updateLocal(listOf(adminPerson))
        if(useDefaultPermissions) {
            AddDefaultSchoolPermissionGrantsUseCase(
                schoolDb = schoolDatabase,
                uidNumberMapper = numMapper,
            ).invoke()
        }
    }


    override fun provideXapiResource(
        endpoint: String,
        authentication: String?
    ): XapiResource {
        return schoolDataSource.xapiResource
    }

}