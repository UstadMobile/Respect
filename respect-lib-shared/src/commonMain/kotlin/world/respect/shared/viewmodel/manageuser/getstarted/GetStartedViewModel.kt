package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.datalayer.opds.model.LangMapStringValue
import world.respect.datalayer.respect.model.RespectRealm
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.generated.resources.school_not_exist_error
import world.respect.shared.navigation.JoinClazzWithCode
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOption
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


class GetStartedViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(GetStartedUiState())
    val uiState = _uiState.asStateFlow()
    private val schoolList = listOf(
        buildSchool("respect school", "ustadtesting.ustadmobile.com"),
        buildSchool("respect 2 school", "respect2.com"),
        buildSchool("spix school", "spix.com"),
        buildSchool("ustad school", "ustad.com")
    )

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.lets_get_started.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = false,
            )
        }
    }

    fun onSchoolNameChanged(name: String) {
        val suggestions = if (name.isBlank()) {
            emptyList()
        } else {
            schoolList.filter { it.name.contains(name, ignoreCase = true) }
        }

        _uiState.update {
            it.copy(
                schoolName = name,
                errorMessage = if (suggestions.isEmpty())
                    StringResourceUiText(Res.string.school_not_exist_error) else null,
                suggestions = suggestions,
                showButtons = suggestions.isEmpty()
            )
        }
    }

    fun onClickIHaveCode() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(JoinClazzWithCode)
        )
    }

    fun onSchoolSelected(school: School) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LoginScreen.create(school.realm.self,school.realm.rpId)
            )
        )
    }

    fun onClickOtherOptions() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(OtherOption))
    }

}
private fun buildSchool(name: String, domain: String): School {
    return School(
        name = name,
        realm = RespectRealm(
            name = LangMapStringValue("Respect Demo School"),
            self = Url("https://$domain"),
            xapi = Url("https://$domain/xapi"),
            oneRoster = Url("https://$domain/oneroster"),
            respectExt = Url("https://$domain/respect"),
            rpId = domain
        )
    )
}
data class School(
    val name: String,
    val realm: RespectRealm
)
data class GetStartedUiState(
    val schoolName: String = "",
    val errorText: String? = null,
    val showButtons: Boolean = true,
    val errorMessage: StringResourceUiText? = null,
    val suggestions: List<School> = emptyList()

)
