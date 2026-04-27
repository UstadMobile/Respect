package world.respect.shared.viewmodel.manageuser.acceptinvite


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ext.isChildUser
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.lib.opds.model.LangMap
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.invite.InviteRecipientType
import world.respect.shared.domain.account.invite.RedeemInviteExistingUserUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest.PersonInfo
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.domain.navigation.inviteforexistingusernavigation.NavigateOnExistingUserInviteAcceptedUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invitation
import world.respect.shared.generated.resources.something_wrong_with_invite
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class AcceptInviteUiState(
    val inviteInfo: RespectInviteInfo? = null,
    val errorText: UiText? = null,
    val isTeacherInvite: Boolean = false,
    val schoolName: LangMap? = null,
    val schoolUrl: Url? = null,
    val uid: String? = null,
    val persons: DataLoadState<List<Person>> = DataLoadingState(),
    val selectedChildGuid: String? = null,
    val showSelectChildDropDown: Boolean = false,
    val childError: UiText? = null,
    ) {
    val person: Person?
        get() = persons.dataOrNull()?.firstOrNull { it.guid == uid }

    val familyMembers: List<Person>
        get() = persons.dataOrNull()?.filter { it.guid != uid } ?: emptyList()

    val children: List<Person>
        get() = familyMembers.filter { member ->
            member.roles.any { it.roleEnum == PersonRoleEnum.STUDENT }
        }
    val selectedChild: Person?
        get() = children.firstOrNull { it.guid == selectedChildGuid }
    val nextButtonEnabled: Boolean
        get() = inviteInfo?.invite != null

}

class AcceptInviteViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val respectAppDataSource: RespectAppDataSource,
    accountManager: RespectAccountManager,
    ) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: AcceptInvite = savedStateHandle.toRoute()

    override val scope: Scope by lazy {
        if (route.personGuid != null) {
            accountManager.requireActiveAccountScope()
        } else {
            getKoin().getOrCreateScope<SchoolDirectoryEntry>(
                SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
            )
        }
    }

    val redeemInviteUseCase: RedeemInviteExistingUserUseCase? by lazy {
        if (route.personGuid != null) {
            scope.get<RedeemInviteExistingUserUseCase>()
        } else {
            null
        }
    }
    private val schoolDataSource: SchoolDataSource by inject()
    private val schoolDataSourceLocal: SchoolDataSourceLocal by inject()
    private val navigateOnExistingUserInviteAcceptedUseCase
    : NavigateOnExistingUserInviteAcceptedUseCase by inject()

    private val getInviteInfoUseCase: GetInviteInfoUseCase = scope.get()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator = scope.get()

    private val _uiState = MutableStateFlow(
        AcceptInviteUiState(schoolUrl = route.schoolUrl, uid = route.personGuid)
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.invitation.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = route.canGoBack,
            )
        }

        launchWithLoadingIndicator(
            onShowError = {
                _uiState.update { it.copy(errorText = Res.string.something_wrong_with_invite.asUiText()) }
            }
        ) {
            val inviteInfo = getInviteInfoUseCase(route.code)

            _uiState.update {
                it.copy(
                    inviteInfo = inviteInfo,
                    isTeacherInvite = false
                )
            }

            if (route.personGuid != null) {
                loadEntity(
                    json = json,
                    serializer = ListSerializer(Person.serializer()),
                    initialStateKey = KEY_INITIAL_STATE,
                    loadFn = { loadParams ->
                        schoolDataSource.personDataSource.list(
                            loadParams = loadParams,
                            params = PersonDataSource.GetListParams(
                                common = GetListCommonParams(
                                    guid = route.personGuid
                                ),
                                includeRelated = true
                            )
                        )
                    },
                    uiUpdateFn = { person ->
                        _uiState.update {
                            prev -> prev.copy(
                                persons = person,
                                showSelectChildDropDown = (uiState.value.person?.roles?.firstOrNull()
                                    ?.roleEnum == PersonRoleEnum.PARENT && uiState.value.children.isNotEmpty())
                            )
                        }
                    }
                )
            }
        }

        viewModelScope.launch {
            val schoolDirEntry = respectAppDataSource.schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
                route.schoolUrl
            ).dataOrNull() ?: return@launch

            _uiState.update {
                it.copy(schoolName = schoolDirEntry.name)
            }
        }
    }
    fun onChildSelected(child: Person) {
        _uiState.update {
            it.copy(
                selectedChildGuid = child.guid,
                childError = null
            )
        }
    }
    fun onClickNext() {
        val invite = uiState.value.inviteInfo?.invite ?: return

        val inviteRedeemRequest = RespectRedeemInviteRequest(
            code = invite.code,
            accountPersonInfo = PersonInfo(),
            account = RespectRedeemInviteRequest.Account(
                guid = uiState.value.person?.guid ?: schoolPrimaryKeyGenerator.primaryKeyGenerator
                    .nextId(Person.TABLE_ID).toString(),
                username = "",
                credential = RespectPasswordCredential(username = "", password = ""),
            ),
            deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
            deviceInfo = getDeviceInfoUseCase(),
            invite = invite,
            recipientType = if (route.personGuid!=null) {
                InviteRecipientType.EXISTING_USER
            } else {
                InviteRecipientType.NEW_USER
            }
        )

        if (route.personGuid!=null&&redeemInviteUseCase!=null) {
            viewModelScope.launch {
              redeemInviteUseCase?.invoke(
                  inviteRedeemRequest,
                  uiState.value.selectedChildGuid
              )

                navigateOnExistingUserInviteAcceptedUseCase(
                    person = uiState.value.person,
                    inviteRequest = inviteRedeemRequest,
                    navCommandFlow = _navCommandFlow
                )
            }
          return
        }
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = if(!invite.isChildUser()) {
                    TermsAndCondition.create(
                        schoolUrl = route.schoolUrl,
                        inviteRequest = inviteRedeemRequest,
                    )
                }else {
                    SignupScreen.create(
                        schoolUrl = route.schoolUrl,
                        inviteRequest = inviteRedeemRequest,
                    )
                }
            )
        )
    }

}
