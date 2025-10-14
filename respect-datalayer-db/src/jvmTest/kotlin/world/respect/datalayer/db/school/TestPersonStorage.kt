package world.respect.datalayer.db.school

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestPersonStorage {


    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var schoolDb:  RespectSchoolDatabase

    private lateinit var schoolDataSource: SchoolDataSource


    @BeforeTest
    fun setup() {
        val dbDir = temporaryFolder.newFolder()
        schoolDb = Room.databaseBuilder<RespectSchoolDatabase>(
            File(dbDir, "school.db").absolutePath,
        ).setDriver(BundledSQLiteDriver())
            .build()

        val uidNumberMapper= XXHashUidNumberMapper(XXStringHasherCommonJvm())
        schoolDataSource = SchoolDataSourceDb(
            schoolDb = schoolDb,
            uidNumberMapper = uidNumberMapper,
            authenticatedUser = AuthenticatedUserPrincipalId("1")
        )
    }

    @Test
    fun givenPersonStoredWithRolesAndRelatedPersons_whenStored_thenShouldMatch() {
        val parentGuid = "2"
        val childGuid = "3"

        val parentPerson = Person(
            guid = parentGuid,
            givenName = "Parent",
            familyName = "Person",
            gender = PersonGenderEnum.FEMALE,
            roles = listOf(PersonRole(true, PersonRoleEnum.PARENT)),
            relatedPersonUids = listOf(childGuid)
        )

        val childPerson = Person(
            guid = childGuid,
            givenName = "Child",
            familyName = "Person",
            gender = PersonGenderEnum.FEMALE,
            roles = listOf(PersonRole(true, PersonRoleEnum.STUDENT)),
            relatedPersonUids = listOf(parentGuid),
        )

        fun assertDetailsMatch(expected: Person, actual: Person?) {
            assertContentEquals(expected.relatedPersonUids, actual?.relatedPersonUids)
            assertEquals(expected.givenName, actual?.givenName)
            assertContentEquals(expected.roles, actual?.roles)
        }

        runBlocking {
            schoolDataSource.personDataSource.store(listOf(parentPerson, childPerson))
            val parentFromDb = schoolDataSource.personDataSource.findByGuid(DataLoadParams(), parentGuid).dataOrNull()
            val childFromDb = schoolDataSource.personDataSource.findByGuid(DataLoadParams(), childGuid).dataOrNull()
            assertDetailsMatch(parentPerson, parentFromDb)
            assertDetailsMatch(childPerson, childFromDb)
        }
    }


}