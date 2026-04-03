package world.respect.datalayer.repository.school

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.server.routing.route
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.ext.foldToFlag
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.lib.test.clientservertest.clientServerDatasourceTest
import world.respect.server.routes.school.respect.SchoolConfigSettingRoute
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SchoolConfigSettingIntegrationTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @BeforeTest
    fun setup() {
        Napier.base(DebugAntilog())
    }

    private val teacherUser = Person(
        guid = "teacher-1",
        givenName = "Teacher",
        familyName = "One",
        gender = PersonGenderEnum.UNSPECIFIED,
        roles = listOf(PersonRole(true, PersonRoleEnum.TEACHER))
    )

    @Test
    fun givenAdminUser_whenStoreSchoolConfigSetting_thenDataIsPersisted() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val testSetting = SchoolConfigSetting(
                    key = "test-key",
                    value = "test-value",
                    canRead = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR),
                    canWrite = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR)
                )

                val adminUidNum = stringHasher.hash(adminUserId.guid)
                serverDb.getSchoolConfigSettingEntityDao()
                    .getLastModifiedAndHasPermission(
                        authenticatedPersonUidNum = adminUidNum,
                        key = testSetting.key,
                        canWriteRolesMask = testSetting.canWrite.foldToFlag()
                    )
                
                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(testSetting))

                val directEntity = serverDb.getSchoolConfigSettingEntityDao().list(
                    keys = listOf(testSetting.key),
                    authenticatedPersonUidNum = stringHasher.hash(adminUserId.guid),
                    since = 0
                )
                assertNotNull(directEntity, "Entity should exist in DB")
                assertEquals(testSetting.value, directEntity.firstOrNull()?.scsValue)
            }
        }
    }

    @Test
    fun givenTeacherRole_whenRequestingAdminOnlySetting_thenNoDataReturned() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-neg")) {
                val teacherPrincipal = AuthenticatedUserPrincipalId(teacherUser.guid)
                
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()


                val adminOnlySetting = SchoolConfigSetting(
                    key = "admin-only",
                    value = "secret",
                    canRead = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR),
                    canWrite = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR)
                )
                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(adminOnlySetting))


                val teacherDbAndSource = newLocalSchoolDatabase(
                    temporaryFolder.newFolder("teacher-db"),
                    stringHasher,
                    teacherPrincipal
                )
                val teacherLocalSource = teacherDbAndSource.second

                teacherLocalSource.personDataSource.updateLocal(listOf(teacherUser))

                val teacherResult = teacherLocalSource.schoolConfigSettingDataSource.list(
                    params = SchoolConfigSettingDataSource.GetListParams(keys = listOf(adminOnlySetting.key))
                )

                assertTrue(teacherResult.dataOrNull()?.isEmpty() ?: true, "Teacher should not be able to read admin-only setting")
            }
        }
    }

    @Test
    fun givenTeacherRole_whenTryingToStoreAdminOnlySetting_thenForbiddenExceptionThrown() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-forbidden")) {
                val teacherPrincipal = AuthenticatedUserPrincipalId(teacherUser.guid)
                
                val teacherDbAndSource = newLocalSchoolDatabase(
                    temporaryFolder.newFolder("teacher-store-db"),
                    stringHasher,
                    teacherPrincipal
                )
                val teacherLocalSource = teacherDbAndSource.second
                teacherLocalSource.personDataSource.updateLocal(listOf(teacherUser))

                val adminOnlySetting = SchoolConfigSetting(
                    key = "admin-only-write",
                    value = "attempt",
                    canRead = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR),
                    canWrite = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR)
                )

                assertFailsWith<ForbiddenException> {
                    teacherLocalSource.schoolConfigSettingDataSource.store(listOf(adminOnlySetting))
                }
            }
        }
    }


    @Test
    fun givenClientStoresSetting_whenDrained_thenServerHasTheData() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-writequeue")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val client = clients.first()
                client.insertServerAdminAndDefaultGrants()

                val testSetting = SchoolConfigSetting(
                    key = "client-key",
                    value = "client-value",
                    canRead = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR),
                    canWrite = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR)
                )

                client.schoolDataSource.schoolConfigSettingDataSource.store(listOf(testSetting))

                delay(2000)

                val serverEntity = serverDb.getSchoolConfigSettingEntityDao().list(
                    keys = listOf(testSetting.key),
                    authenticatedPersonUidNum = stringHasher.hash(adminUserId.guid),
                    since = 0
                )
                assertNotNull(serverEntity.firstOrNull(), "Data should have been synced to server")
                assertEquals("client-value", serverEntity.firstOrNull()?.scsValue)
            }
        }
    }
}
