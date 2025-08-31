package world.respect.shared.domain.account

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.respect.model.RESPECT_SCHOOL_JSON_PATH
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase
import world.respect.shared.domain.school.MakeSchoolPathDirUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.isSameAccount

/**
 *
 * @property accounts all the accounts that the user has signed in. The user can use the account
 *           switcher to switch between them.
 * @property selectedAccount The selected account is the stored account that the user has currently
 *           selected (eg the one normally displayed in the top right). The selectedAccount is null
 *           if the user has not signed in yet.
 */
class RespectAccountManager(
    private val settings: Settings,
    private val json: Json,
    private val tokenManager: RespectTokenManager,
    private val httpClient: HttpClient,
): KoinComponent {

    private val _storedAccounts = MutableStateFlow<List<RespectAccount>>(
        value = settings.getStringOrNull(SETTINGS_KEY_STORED_ACCOUNTS)?.let {
            json.decodeFromString(it)
        } ?: emptyList()
    )

    val accounts = _storedAccounts.asStateFlow()

    private val _selectedAccountGuid = MutableStateFlow(
        settings.getStringOrNull(SETTINGS_KEY_ACTIVE_ACCOUNT)
    )

    var selectedAccount: RespectAccount?
        get() = _storedAccounts.value.firstOrNull {
            it.userGuid == _selectedAccountGuid.value
        }

        set(value) {
            if(value == null) {
                _selectedAccountGuid.value = null
                settings.remove(SETTINGS_KEY_ACTIVE_ACCOUNT)
            }else {
                if(!_storedAccounts.value.any { it.userGuid == value.userGuid }) {
                    val newValue = _storedAccounts.updateAndGet { prev ->
                        listOf(value) + prev
                    }
                    settings[SETTINGS_KEY_STORED_ACCOUNTS] = json.encodeToString(newValue)
                }

                _selectedAccountGuid.value = value.userGuid
                settings.set(SETTINGS_KEY_ACTIVE_ACCOUNT, value.userGuid)
            }
        }

    val activeAccountFlow: Flow<RespectAccount?> = _storedAccounts.combine(
        _selectedAccountGuid
    ) { accountList, activeAccountSourcedId ->
        accountList.firstOrNull { it.userGuid == activeAccountSourcedId }
    }

    val activeAccountAndPersonFlow: Flow<RespectAccountAndPerson?> = channelFlow {
        activeAccountFlow.collectLatest { account ->
            val person = if(account != null) {
                val accountScope = getOrCreateAccountScope(account)
                val schoolDataSource: SchoolDataSource = accountScope.get()
                schoolDataSource.personDataSource.findByGuid(
                    DataLoadParams(onlyIfCached = true), account.userGuid
                ).dataOrNull()
            }else {
                null
            }

            if(account != null && person != null) {
                send(RespectAccountAndPerson(account, person))
            }else {
                send(null)
            }
        }
    }

    /**
     * Login a user with the given credentials
     */
    suspend fun login(
        username: String,
        password: String,
        schoolUrl: Url,
    ) {
        val schoolScopeId = SchoolDirectoryEntryScopeId(schoolUrl, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            schoolScopeId.scopeId
        )

        val authUseCase: GetTokenAndUserProfileWithUsernameAndPasswordUseCase = schoolScope.get()
        val authResponse = authUseCase(
            username = username,
            password = password,
        )

        //This could/should move to using the datasource instead of httpclient directly
        val schoolDirectoryEntry: SchoolDirectoryEntry = httpClient.get(
            schoolUrl.resolve(RESPECT_SCHOOL_JSON_PATH)
        ).body()

        val respectAccount = RespectAccount(
            userGuid = authResponse.person.guid,
            school = schoolDirectoryEntry,
        )

        initSession(authResponse, respectAccount)
    }

    private suspend fun initSession(
        authResponse: AuthResponse,
        respectAccount: RespectAccount,
    ) {
        val schoolScope: Scope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            respectAccount.school.self.toString()
        )
        tokenManager.storeToken(respectAccount.scopeId, authResponse.token)

        val accountScope = getOrCreateAccountScope(respectAccount)

        val schoolDataSource: SchoolDataSource = accountScope.get()
        (schoolDataSource.personDataSource as? PersonDataSourceLocal)?.putPersonsLocal(
            listOf(authResponse.person))

        //now we can get the datalayer by creating a RespectAccount scope
        val mkDirUseCase: MakeSchoolPathDirUseCase? = schoolScope.getOrNull()
        mkDirUseCase?.invoke()

        selectedAccount = respectAccount
    }


    @Suppress("RedundantSuspendModifier") //Likely needs to be suspended to communicate to server
    suspend fun endSession(account: RespectAccount) {
        if(selectedAccount?.isSameAccount(account) == true) {
            selectedAccount = null
        }

        val accountScope = getOrCreateAccountScope(account)
        accountScope.close()

        tokenManager.removeToken(account.scopeId)

        val storedAccountsToCommit = _storedAccounts.updateAndGet { prev ->
            prev.filterNot {
                it.isSameAccount(account)
            }
        }

        settings[SETTINGS_KEY_STORED_ACCOUNTS] = json.encodeToString(storedAccountsToCommit)

        val accountsOnRealmScope = accounts.value.count {
            it.school.self == account.school.self
        }

        if(accountsOnRealmScope == 0) {
            //close it
            val realmScope = getKoin().getScope(account.school.self.toString())
            realmScope.close()
        }

    }

    /**
     * When the RespectAccount scope is created it MUST be linked to the parent Realm scope.
     */
    fun getOrCreateAccountScope(account: RespectAccount): Scope {
        val schoolScopeId = SchoolDirectoryEntryScopeId(account.school.self, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(schoolScopeId.scopeId)
        val accountScope = getKoin().getScopeOrNull(account.scopeId)
            ?: getKoin().createScope<RespectAccount>(account.scopeId).also {
                it.linkTo(schoolScope)
            }

        return accountScope
    }

    fun requireSelectedAccountScope(): Scope {
        return selectedAccount?.let { getOrCreateAccountScope(it) }
            ?: throw IllegalStateException("require scope for selected account: no account selected")
    }



    companion object {

        const val SETTINGS_KEY_STORED_ACCOUNTS = "accountmanager-storedaccounts"

        const val SETTINGS_KEY_ACTIVE_ACCOUNT = "accountmanager-activeaccount"
    }
}