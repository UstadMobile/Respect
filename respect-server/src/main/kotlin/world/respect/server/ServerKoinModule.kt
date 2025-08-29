package world.respect.server
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.http.Url
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectAppDataSourceDb
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.server.domain.school.add.AddSchoolDirectoryCallback
import world.respect.server.domain.school.add.AddSchoolUseCase
import world.respect.server.domain.school.add.AddServerManagedDirectoryCallback
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithUsernameAndPasswordDbImpl
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseDbImpl
import world.respect.shared.domain.school.RespectSchoolPath
import java.io.File

const val APP_DB_FILENAME = "respect-app.db"

fun serverKoinModule(
    config: ApplicationConfig,
) = module {

    val dataDir = config.absoluteDataDir()

    single<RespectAppDatabase> {
        val dbFile = File(dataDir, APP_DB_FILENAME)
        Room.databaseBuilder<RespectAppDatabase>(dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .addCallback(AddServerManagedDirectoryCallback(xxStringHasher = get()))
            .addCallback(AddSchoolDirectoryCallback(xxStringHasher = get()))
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

    single<RespectAppDataSource> {
        RespectAppDataSourceDb(
            respectAppDatabase = get(),
            json = get(),
            xxStringHasher = get(),
            primaryKeyGenerator = get(),
        )
    }

    single<AddSchoolUseCase> {
        AddSchoolUseCase(
            directoryDataSource = get<RespectAppDataSource>().schoolDirectoryDataSource as SchoolDirectoryDataSourceLocal
        )
    }

    /*
     * School scope: scope id = the full school url as per SchoolDirectoryEntry.self
     */
    scope<SchoolDirectoryEntry> {
        scoped<RespectSchoolPath> {
            val schoolDirName = Url(id).sanitizedForFilename()
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
                appDb.getSchoolConfigEntityDao().findByUid(xxHasher.hash(id))
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

        scoped<SchoolDataSourceLocal> {
            SchoolDataSourceDb(schoolDb = get(), xxStringHasher = get())
        }

        scoped<SchoolDataSource> {
            get<SchoolDataSourceLocal>()
        }

        scoped<GetTokenAndUserProfileWithUsernameAndPasswordUseCase> {
            GetTokenAndUserProfileWithUsernameAndPasswordDbImpl(
                schoolDb = get(),
                xxHash = get(),
                personDataSource = get<SchoolDataSource>().personDataSource,
            )

        }
    }

}