package world.respect.server
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.http.Url
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.core.scope.Scope
import org.koin.dsl.module
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectAppDataSourceDb
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.schooldirectory.SchoolDirectoryDataSourceDb
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.server.account.invite.GetInviteInfoUseCaseServer
import world.respect.server.domain.school.add.AddSchoolUseCase
import world.respect.server.domain.school.add.AddServerManagedDirectoryCallback
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithUsernameAndPasswordDbImpl
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseDbImpl
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCaseDbImpl
import world.respect.shared.domain.mock.MockGetInviteInfoUseCase
import world.respect.shared.domain.school.RespectSchoolPath
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import java.io.File

const val APP_DB_FILENAME = "respect-app.db"

fun serverKoinModule(
    config: ApplicationConfig,
    dataDir: File = config.absoluteDataDir()
) = module {

    single<RespectAppDatabase> {
        val dbFile = File(dataDir, APP_DB_FILENAME)
        Room.databaseBuilder<RespectAppDatabase>(dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .addCallback(AddServerManagedDirectoryCallback(xxStringHasher = get()))
            .build()
    }

    single<Json> {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single<XXStringHasher> {
        XXStringHasherCommonJvm()
    }

    single<PrimaryKeyGenerator> {
        PrimaryKeyGenerator(RespectAppDatabase.TABLE_IDS)
    }
    single<SchoolDirectoryDataSourceLocal> {
        SchoolDirectoryDataSourceDb(
            respectAppDb = get(),
            json = get(),
            xxStringHasher = get()
        )
    }
    single<RespectAppDataSource> {
        RespectAppDataSourceDb(
            respectAppDatabase = get(),
            json = get(),
            xxStringHasher = get(),
            primaryKeyGenerator = get(),
        )
    }

    single<GetInviteInfoUseCase> {
        MockGetInviteInfoUseCase(
        )
    }

    single<AddSchoolUseCase> {
        AddSchoolUseCase(
            directoryDataSource = get<RespectAppDataSource>().schoolDirectoryDataSource as SchoolDirectoryDataSourceLocal
        )
    }

    single<GetInviteInfoUseCase> {
        GetInviteInfoUseCaseServer(
            respectAppDb = get()
        )
    }

    /*
     * School scope: used as the basis for virtual hosting.
     */
    scope<SchoolDirectoryEntry> {
        fun Scope.schoolUrl(): Url = SchoolDirectoryEntryScopeId.parse(id).schoolUrl

        scoped<RespectSchoolPath> {
            val schoolDirName = schoolUrl().sanitizedForFilename()
            val schoolDirFile = File(dataDir, schoolDirName).also {
                if(!it.exists())
                    it.mkdirs()
            }

            RespectSchoolPath(
                path = Path(schoolDirFile.absolutePath)
            )
        }

        scoped<RespectSchoolDatabase> {
            val schoolPath: RespectSchoolPath = get()
            val appDb: RespectAppDatabase = get()
            val xxHasher: XXStringHasher = get()

            val schoolConfig = runBlocking {
                appDb.getSchoolConfigEntityDao().findByUid(xxHasher.hash(schoolUrl().toString()))
            } ?: throw IllegalStateException("School config not found for $id")

            val schoolConfigFile = File(schoolPath.path.toString())
            val dbFile = schoolConfigFile.resolve(schoolConfig.dbUrl)

            Room.databaseBuilder<RespectSchoolDatabase>(dbFile.absolutePath)
                .setDriver(BundledSQLiteDriver())
                .build()
        }

        scoped<SetPasswordUseCase> {
            SetPasswordUseDbImpl(
                schoolDb = get(),
                xxHash = get()
            )
        }

        scoped<ValidateAuthorizationUseCase> {
            ValidateAuthorizationUseCaseDbImpl(schoolDb = get())
        }

        scoped<GetTokenAndUserProfileWithUsernameAndPasswordUseCase> {
            GetTokenAndUserProfileWithUsernameAndPasswordDbImpl(
                schoolDb = get(),
                xxHash = get(),
            )
        }
    }

    /*
     * AccountScope: as per the client, the Account Scope is linked to a parent School scope.
     *
     * All server-side dependencies in the account scope are cheap wrappers e.g. the
     * SchoolDataSource wrapper (which is tied to a specific account guid) is kept in the AccountScope,
     * but the RespectSchoolDatabase which has the actual DB connection is kept in the school scope.
     *
     * Dependencies in the account scope use factory so they are not retained in memory
     */
    scope<RespectAccount> {
        factory<SchoolDataSourceLocal> {
            SchoolDataSourceDb(
                schoolDb = get(),
                xxStringHasher = get(),
                authenticatedUser = RespectAccountScopeId.parse(id).accountPrincipalId
            )
        }

        factory<SchoolDataSource> {
            get<SchoolDataSourceLocal>()
        }
    }


}