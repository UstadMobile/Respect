package world.respect.datalayer.db.school

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File

suspend fun testSchoolDb(
    tempDir: File,
    dbFileName: String = "school.db",
    block: suspend (RespectSchoolDatabase) -> Unit,
) {
    val db = Room.databaseBuilder<RespectSchoolDatabase>(
        File(tempDir, dbFileName).absolutePath
    ).setDriver(BundledSQLiteDriver())
        .build()

    try {
        block(db)
    }finally {
        db.close()
    }
}

suspend fun SchoolDataSourceDb.insertAdmin(
    adminUserUid: String = "1"
) : Person{
    val adminPerson = Person(
        guid = adminUserUid,
        givenName = "Admin",
        familyName = "User",
        gender = PersonGenderEnum.FEMALE,
        roles = listOf(
            PersonRole(
                isPrimaryRole = true,
                roleEnum = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
            )
        ),
    )
    personDataSource.updateLocal(listOf(adminPerson))
    return adminPerson
}

fun RespectSchoolDatabase.toDataSource(
    authenticatedUserUid: String,
    uidNumberMapper: UidNumberMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm()),
): SchoolDataSourceDb {
    val authenticatedUser = AuthenticatedUserPrincipalId(authenticatedUserUid)
    return SchoolDataSourceDb(
        schoolDb = this,
        uidNumberMapper = uidNumberMapper,
        authenticatedUser = authenticatedUser,
        checkPersonPermissionUseCase = CheckPersonPermissionUseCaseDbImpl(
            authenticatedUser = authenticatedUser,
            schoolDb = this,
            uidNumberMapper = uidNumberMapper,
        )
    )
}



