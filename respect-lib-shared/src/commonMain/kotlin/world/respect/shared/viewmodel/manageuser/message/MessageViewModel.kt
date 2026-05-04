package world.respect.shared.viewmodel.manageuser.message

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_link_message
import world.respect.shared.generated.resources.link_is
import world.respect.shared.generated.resources.message
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.Message
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class MessageUiState(
    val schoolUrl: Url? = null,
    val link: String? = null,
)

class MessageViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val route: Message = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(
        MessageUiState(link = route.link
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.message.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = route.canGoBack,
            )
        }



    }
    fun onClickLink() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AcceptInvite.create(
                    schoolUrl = route.schoolUrl,
                    code = route.code,
                    canGoBack = route.canGoBack,
                ), clearBackStack = false
            )
        )
    }

}