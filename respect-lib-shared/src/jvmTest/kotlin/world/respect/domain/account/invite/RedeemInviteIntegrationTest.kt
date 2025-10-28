package world.respect.domain.account.invite

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.test.clientservertest.clientServerDatasourceTest
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
import world.respect.shared.domain.account.invite.RedeemInviteUseCaseDb
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCaseImpl
import world.respect.sharedse.domain.account.authenticatepassword.AuthenticatePasswordUseCaseDbImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class RedeemInviteIntegrationTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun givenValidInviteRedeemCode_whenInviteRequestSubmitted_thenUserAndEnrollmentsCreated() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-invite")) {
                serverRouting {

                }


                server.start()
                val xxStringHasher = XXStringHasherCommonJvm()

                val redeemInviteUseCaseServer = RedeemInviteUseCaseDb(
                    schoolDb = serverSchoolSourceAndDb.first,
                    schoolUrl = schoolUrl,
                    schoolPrimaryKeyGenerator = serverSchoolPrimaryKeyGenerator,
                    getTokenAndUserProfileUseCase = GetTokenAndUserProfileWithCredentialDbImpl(
                        schoolDb = serverSchoolSourceAndDb.first,
                        xxHash = xxStringHasher,
                        schoolUrl = schoolUrl,
                        respectAppDataSource = mock {  },
                        verifyPasskeyUseCase = mock {  },
                        authenticatePasswordUseCase = AuthenticatePasswordUseCaseDbImpl(
                            schoolDb = serverSchoolSourceAndDb.first,
                            encryptPersonPasswordUseCase = EncryptPersonPasswordUseCaseImpl(),
                            uidNumberMapper = XXHashUidNumberMapper(xxStringHasher),
                        )
                    ),
                    schoolDataSource = { _, _ -> serverSchoolDataSource },
                    uidNumberMapper = XXHashUidNumberMapper(xxStringHasher),
                    json = json,
                    getPasskeyProviderInfoUseCase = mock { },
                    encryptPersonPasswordUseCase = EncryptPersonPasswordUseCaseImpl(),
                )

                val clazz = Clazz(
                    guid = "42",
                    title = "Test class",
                    teacherInviteCode = "67890",
                    studentInviteCode = "91000",
                )

                serverSchoolDataSource.classDataSource.store(listOf(clazz))

                val request = RespectRedeemInviteRequest(
                    code = "123-456-${clazz.teacherInviteCode}",
                    classUid = clazz.guid,
                    accountPersonInfo = RespectRedeemInviteRequest.PersonInfo(
                        name = "Edna Kr",
                        gender = PersonGenderEnum.FEMALE,
                        dateOfBirth = LocalDate.parse("1997-05-01"),
                    ),
                    role = PersonRoleEnum.TEACHER,
                    parentOrGuardianRole = null,
                    account = RespectRedeemInviteRequest.Account(
                        guid = "1",
                        username = "username",
                        credential = RespectPasswordCredential(
                            "username", "bart"
                        ),
                    )
                )

                val result = redeemInviteUseCaseServer(request)

                val person = serverSchoolDataSource.personDataSource.findByGuid(
                    DataLoadParams(), result.person.guid
                )

                assertEquals(request.account.username, person.dataOrNull()?.username)
            }
        }
    }

}