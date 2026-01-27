package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.sharefeedback.model.FeedbackTicket
import world.respect.datalayer.sharefeedback.FeedBackDataSource.Companion.DEFAULT_GROUP_ID
import world.respect.datalayer.sharefeedback.model.Article
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchers.EmailLauncher
import world.respect.shared.domain.launchers.WebLauncher
import world.respect.shared.domain.launchers.WhatsAppLauncher
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.category_integrated_apps
import world.respect.shared.generated.resources.category_launcher
import world.respect.shared.generated.resources.category_other
import world.respect.shared.generated.resources.category_question
import world.respect.shared.generated.resources.category_rate_us
import world.respect.shared.generated.resources.feedback_respect
import world.respect.shared.generated.resources.guess
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

    var subject = ""

    private val schoolDataSource: SchoolDataSource by inject()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.share_feedback.asUiText(),
            )
        }
        viewModelScope.launch {
            subject = getString(Res.string.feedback_respect)

            val categoryList: List<String> = listOf(
                getString(Res.string.category_launcher),
                getString(Res.string.category_integrated_apps),
                getString(Res.string.category_question),
                getString(Res.string.category_rate_us),
                getString(Res.string.category_other)
            )

            _uiState.update { currentState ->
                currentState.copy(
                    categories = categoryList,
                    selectedCategory = categoryList.first()
                )
            }
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
            emailLauncher.sendEmail(subject)
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

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onClickSubmit() {
        viewModelScope.launch {
            val ticket = FeedbackTicket(
                title = _uiState.value.selectedCategory,
                groupId = DEFAULT_GROUP_ID,
                customerId = "${getString(Res.string.guess)}${_uiState.value.email}",
                article = Article(
                    subject = subject,
                    body = _uiState.value.feedbackText,
                )
            )
            schoolDataSource.feedBackDataSource.createTicket(ticket)
        }
    }
}
