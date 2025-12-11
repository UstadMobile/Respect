package world.respect.shared.domain.account

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.RespectCredential
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.libutil.util.putDebugCrashCustomData
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.school.MakeSchoolPathDirUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.isSameAccount

/**
 *
 * @property accounts all the accounts that the user has signed in. The user can use the account
 *           switcher to switch between them.
 */
class RespectAccountManager(
    private val settings: Settings,
    private val json: Json,
    private val tokenManager: RespectTokenManager,
    private val appDataSource: RespectAppDataSource,
): KoinComponent {

    private val _storedAccounts = MutableStateFlow<List<RespectAccount>>(
        value = settings.getStringOrNull(SETTINGS_KEY_STORED_ACCOUNTS)?.let {
            json.decodeFromString(it)
        } ?: emptyList()
    )

    val accounts = _storedAccounts.asStateFlow()

    private val _activeSession = MutableStateFlow(
        settings.getStringOrNull(SETTINGS_KEY_ACTIVE_SESSION)?.let {
            json.decodeFromString(RespectSession.serializer(), it)
        }
    )

    /**
     * The active account is the stored account that the user has currently
     * selected (eg the one normally displayed in the top right). The active account is null
     * if the user is not currently signed in.
     */
    val activeAccount: RespectAccount?
        get() {
            val activeSessionVal = _activeSession.value ?: return null
            return _storedAccounts.value.firstOrNull {
                it.isSameAccount(activeSessionVal.account)
            }
        }

    val selectedAccountFlow: Flow<RespectAccount?> = _storedAccounts.combine(
        _activeSession
    ) { accountList, activeSession ->
        if(activeSession == null) {
            null
        } else {
            accountList.firstOrNull { activeSession.account.isSameAccount(it) }
        }
    }

    val selectedAccountAndPersonFlow: Flow<RespectAccountAndPerson?> = channelFlow {
        selectedAccountFlow.collectLatest { account ->
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

    init {
        putDebugCrashCustomData("SelectedAccount", activeAccount.toString())
    }

    /**
     * Login a user with the given credentials
     */
    suspend fun login(
        credential: RespectCredential,
        schoolUrl: Url,
    ) {
        val schoolScopeId = SchoolDirectoryEntryScopeId(schoolUrl, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            schoolScopeId.scopeId
        )

        val authUseCase: GetTokenAndUserProfileWithCredentialUseCase = schoolScope.get()
        val authResponse = authUseCase(credential)

        val schoolDirectoryEntry = appDataSource.schoolDirectoryEntryDataSource
            .getSchoolDirectoryEntryByUrl(schoolUrl)
            .dataOrNull() ?: throw IllegalStateException()

        val respectAccount = RespectAccount(
            userGuid = authResponse.person.guid,
            school = schoolDirectoryEntry,
        )

        initSession(authResponse, RespectSession(respectAccount, null))
    }

    @Suppress("unused")
    suspend fun register(
        redeemInviteRequest: RespectRedeemInviteRequest,
        schoolUrl: Url,
    ) {
        val schoolScopeId = SchoolDirectoryEntryScopeId(
            schoolUrl, null,
        )
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            schoolScopeId.scopeId
        )

        val redeemInviteUseCase: RedeemInviteUseCase = schoolScope.get()
        val authResponse = redeemInviteUseCase(redeemInviteRequest)

        val schoolDirectoryEntry = appDataSource.schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
            schoolUrl
        ).dataOrNull() ?: throw IllegalStateException()

        initSession(
            authResponse = authResponse,
            session = RespectSession(
                account = RespectAccount(
                    authResponse.person.guid, schoolDirectoryEntry
                ),
                profilePersonUid = null,
            )
        )
    }

    private suspend fun initSession(
        authResponse: AuthResponse,
        session: RespectSession,
    ) {
        val schoolScope: Scope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(
                schoolUrl = session.account.school.self, accountPrincipalId = null,
            ).scopeId
        )
        tokenManager.storeToken(session.account.scopeId, authResponse.token)

        val accountScope = getOrCreateAccountScope(session.account)

        val schoolDataSource: SchoolDataSource = accountScope.get()

        //Ensure the active user is loaded into the database
        schoolDataSource.personDataSource.findByGuid(
            DataLoadParams(), authResponse.person.guid
        )

        //now we can get the datalayer by creating a RespectAccount scope
        val mkDirUseCase: MakeSchoolPathDirUseCase? = schoolScope.getOrNull()
        mkDirUseCase?.invoke()

        _activeSession.value = session

        if(!_storedAccounts.value.any { it.isSameAccount(session.account) }) {
            val newValue = _storedAccounts.updateAndGet { prev ->
                listOf(session.account) + prev
            }

            settings[SETTINGS_KEY_STORED_ACCOUNTS] = json.encodeToString(newValue)
        }

        settings[SETTINGS_KEY_ACTIVE_SESSION] = json.encodeToString(session)

        putDebugCrashCustomData("SelectedAccount", activeAccount.toString())
    }


    /**
     * Remove the given account from the list of stored accounts. If the given account is also the
     * used for the active session, the session is ended and the active session will be null (e.g.
     * when the user logs out).
     */
    @Suppress("RedundantSuspendModifier") //Likely needs to be suspended to communicate to server
    suspend fun removeAccount(account: RespectAccount) {
        _activeSession.update { prev ->
            if(prev?.account?.isSameAccount(account) == true )
                null
            else
                prev
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

    fun switchAccount(account: RespectAccount) {
        if(!_storedAccounts.value.any { it.isSameAccount(account) }) {
            throw IllegalArgumentException("switchAccount: account not stored/available")
        }

        _activeSession.value = RespectSession(account, null)
    }

    /**
     * When the RespectAccount scope is created it MUST be linked to the parent Realm scope.
     */
    fun getOrCreateAccountScope(account: RespectAccount): Scope {
        return getKoin().getOrCreateScope<RespectAccount>(account.scopeId)
    }

    fun requireActiveAccountScope(): Scope {
        return activeAccount?.let { getOrCreateAccountScope(it) }
            ?: throw IllegalStateException("require scope for selected account: no account selected")
    }



    companion object {

        const val SETTINGS_KEY_STORED_ACCOUNTS = "accountmanager-storedaccounts"

        const val SETTINGS_KEY_ACTIVE_SESSION = "accountmanager-activesession"
    }
}