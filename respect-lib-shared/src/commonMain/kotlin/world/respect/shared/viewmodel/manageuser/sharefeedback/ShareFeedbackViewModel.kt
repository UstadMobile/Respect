package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.sharefeedback.model.FeedbackTicket
import world.respect.datalayer.sharefeedback.FeedBackDataSource
import world.respect.datalayer.sharefeedback.model.Article
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchers.EmailLauncher
import world.respect.shared.domain.launchers.WebLauncher
import world.respect.shared.domain.launchers.WhatsAppLauncher
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.share_feedback
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import kotlin.getValue

data class ShareFeedbackUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val feedbackText: String = "",
    val isCheckBoxSelected: Boolean = false,
    val phoneNumber: String = "",
    val email: String = "",
    val nationalPhoneNumSet: Boolean = false,
)

class ShareFeedbackViewModel(
    accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val emailLauncher: EmailLauncher,
    private val webLauncher: WebLauncher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(ShareFeedbackUiState())

    val uiState = _uiState.asStateFlow()

    private val feedBackDataSource: FeedBackDataSource by inject()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.share_feedback.asUiText(),
            )
        }
        // Initialize the categories list
        val categoryList = listOf(
            "Respect launcher related issues",
            "Integrated Apps related issues",
            "Question",
            "Rate us",
            "Other"
        )

        _uiState.update { currentState ->
            currentState.copy(
                categories = categoryList,
                selectedCategory = categoryList.first()
            )
        }
    }

    fun onFeedbackTextChanged(text: String) {
        _uiState.update { it.copy(feedbackText = text) }
    }

    fun onClickWhatsApp() {
        viewModelScope.launch {
            whatsAppLauncher.launchWhatsApp()
        }
    }

    fun onClickEmail() {
        viewModelScope.launch {
            emailLauncher.sendEmail()
        }
    }

    fun onClickPublicForum() {
        viewModelScope.launch {
            webLauncher.launchWeb()
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category
        )
    }

    fun onClickCheckBox() {
        _uiState.update { prev ->
            prev.copy(
                isCheckBoxSelected = !prev.isCheckBoxSelected
            )
        }
    }

    fun onNationalPhoneNumSetChanged(phoneNumSet: Boolean) {
        _uiState.takeIf { it.value.nationalPhoneNumSet != phoneNumSet }?.update { prev ->
            prev.copy(nationalPhoneNumSet = phoneNumSet)
        }
    }

    fun onClickSubmit() {
        //testing
        val userEmail = "mandvi2346verma@gmail.com"

        val ticket = FeedbackTicket(
            title = "Ticket 1",
            groupId = "1",
            customerId = "guess:$userEmail",
            article = Article(
                subject = "Test Ticket",
                body = "Testing the ticket",
            )

        )
        viewModelScope.launch {
          val response = feedBackDataSource.createTicket(ticket)
        }


    }

}