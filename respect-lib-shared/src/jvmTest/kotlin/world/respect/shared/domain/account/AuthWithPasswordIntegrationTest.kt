package world.respect.shared.domain.account

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.school.model.Person
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithUsernameAndPasswordDbImpl
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseDbImpl
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCaseDbImpl
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthWithPasswordIntegrationTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var schoolDb: RespectSchoolDatabase

    private lateinit var xxHash: XXStringHasher

    private lateinit var uidNumberMapper: UidNumberMapper

    private lateinit var setPasswordUseCase: SetPasswordUseCase

    private lateinit var getTokenUseCase: GetTokenAndUserProfileWithUsernameAndPasswordUseCase

    private lateinit var validateAuthUseCase: ValidateAuthorizationUseCase

    private val defaultTestPerson = Person(
        guid = "42",
        username = "testuser",
        givenName = "John",
        familyName = "Doe",
        roles = emptyList(),
        gender = PersonGenderEnum.FEMALE,
    )

    @BeforeTest
    fun setup() {
        val dbDir = temporaryFolder.newFolder("dbdir")
        schoolDb = Room.databaseBuilder<RespectSchoolDatabase>(
            File(dbDir, "realm-test.db").absolutePath
        ).setDriver(BundledSQLiteDriver())
            .build()
        xxHash = XXStringHasherCommonJvm()
        uidNumberMapper = XXHashUidNumberMapper(xxHash)
        setPasswordUseCase = SetPasswordUseDbImpl(schoolDb, xxHash)
        getTokenUseCase = GetTokenAndUserProfileWithUsernameAndPasswordDbImpl(
            schoolDb, xxHash,
        )

        validateAuthUseCase = ValidateAuthorizationUseCaseDbImpl(schoolDb)
    }

    @Test
    fun givenAuthSet_whenAuthWithPasswordInvoked_thenWillReturnToken() {
        runBlocking {
            val personGuid = "42"
            val password = "password"

            schoolDb.getPersonEntityDao().insert(
                defaultTestPerson.toEntities(uidNumberMapper).personEntity
            )

            setPasswordUseCase(
                SetPasswordUseCase.SetPasswordRequest(
                    authenticatedUserId = AuthenticatedUserPrincipalId(
                        AuthenticatedUserPrincipalId.DIRECTORY_ADMIN_GUID
                    ),
                    userGuid = personGuid,
                    password = password,
                )
            )

            val authResponse = getTokenUseCase(defaultTestPerson.username!!, password)

            val userIdPrincipal = validateAuthUseCase(
                ValidateAuthorizationUseCase.BearerTokenCredential(
                    token = authResponse.token.accessToken
                )
            )

            assertEquals(authResponse.person.guid, personGuid)
            assertEquals(defaultTestPerson.guid, userIdPrincipal!!.guid)
        }
    }

    @Test
    fun givenAuthSet_whenAuthPasswordInvokedWithWRongPass_thenWillThrowException() {
        runBlocking {
            var exception: Throwable? = null
            try {
                val personGuid = "42"
                val password = "password"
                schoolDb.getPersonEntityDao().insert(
                    defaultTestPerson.toEntities(uidNumberMapper).personEntity
                )

                setPasswordUseCase(
                    SetPasswordUseCase.SetPasswordRequest(
                        authenticatedUserId = AuthenticatedUserPrincipalId("foo"),
                        userGuid = personGuid,
                        password = password,
                    )
                )

                getTokenUseCase(defaultTestPerson.username!!, "wrong")
            }catch(e: Throwable) {
                exception = e
            }

            assertNotNull(exception)
        }
    }

}