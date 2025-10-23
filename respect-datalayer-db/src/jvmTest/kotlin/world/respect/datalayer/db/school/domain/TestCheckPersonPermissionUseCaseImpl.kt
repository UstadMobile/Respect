package world.respect.datalayer.db.school.domain

import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.school.GetAuthenticatedPersonUseCase
import world.respect.datalayer.db.school.insertAdmin
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.datalayer.school.ext.primaryRole
import world.respect.datalayer.school.ext.writePermissionFlag
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse

class TestCheckPersonPermissionUseCaseImpl {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenAuthenticatedUserIsStudent_whenCheckWritePermissionForTeacher_thenReturnsFalse() {
        runBlocking {
            testSchoolDb(File("/home/mike/tmp/school-permission.db")) { db ->
                val adminUid = "1"
                val studentUid = "2"
                val adminSchoolDs = db.toDataSource(adminUid)
                val adminPerson = adminSchoolDs.insertAdmin(adminUid)
                adminSchoolDs.personDataSource.store(
                    listOf(
                        Person(
                            guid = studentUid,
                            givenName = "Student",
                            familyName = "User",
                            gender = PersonGenderEnum.FEMALE,
                            roles = listOf(PersonRole(true, PersonRoleEnum.STUDENT)),
                        )
                    )
                )

                AddDefaultSchoolPermissionGrantsUseCase(adminSchoolDs).invoke()
                val checkPersonUseCase = CheckPersonPermissionUseCaseDbImpl(
                    getAuthenticatedPersonUseCase = GetAuthenticatedPersonUseCase(
                        AuthenticatedUserPrincipalId(studentUid),
                        db,
                        XXHashUidNumberMapper(XXStringHasherCommonJvm())
                    ),
                    schoolDb = db,
                    uidNumberMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm())
                )

                assertFalse(checkPersonUseCase(adminPerson, adminPerson.primaryRole().writePermissionFlag))
            }
        }
    }

}