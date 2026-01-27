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
import world.respect.shared.generated.resources.either_number_or_email
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.share_feedback
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import kotlin.getValue

data class ShareFeedbackUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val feedbackDescription: String = "",
    val isCheckBoxSelected: Boolean = false,
    val phoneNumber: String = "",
    val email: String = "",
    val nationalPhoneNumSet: Boolean = false,
    val feedbackDescriptionError: UiText? = null,
    val contactError: UiText? = null,
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

    fun onFeedbackDescriptionChanged(text: String) {
        _uiState.update { it.copy(feedbackDescription = text, feedbackDescriptionError = null) }
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
        _uiState.update { it.copy(phoneNumber = phone, contactError = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, contactError = null) }
    }

    fun onClickSubmit() {
        val feedbackDescriptionError = if (_uiState.value.feedbackDescription.isBlank()) {
            Res.string.required_field.asUiText()
        } else null

        val isContactProvided = _uiState.value.email.isNotBlank() || _uiState.value.phoneNumber.isNotBlank()

        val contactReqError = if (_uiState.value.isCheckBoxSelected && !isContactProvided) {
            Res.string.either_number_or_email.asUiText()
        } else null
        _uiState.update { it.copy(
            feedbackDescriptionError = feedbackDescriptionError,
            contactError = contactReqError
        )}

        viewModelScope.launch {
            val customerEmail = if (_uiState.value.isCheckBoxSelected) {
                _uiState.value.email.ifBlank {
                    "${_uiState.value.phoneNumber}$DEFAULT_CUSTOMER_ENDPOINT"
                }
            } else {
                DEFAULT_CUSTOMER_ID
            }

            val ticket = FeedbackTicket(
                title = _uiState.value.selectedCategory,
                groupId = DEFAULT_GROUP_ID,
                customerId = "$GUESS$customerEmail",
                article = Article(
                    subject = subject,
                    body = _uiState.value.feedbackDescription,
                )
            )

            schoolDataSource.feedBackDataSource.createTicket(ticket)
        }
    }

    companion object{
        const val DEFAULT_CUSTOMER_ENDPOINT= "@ustadmobile.com"
        const val DEFAULT_CUSTOMER_ID= "info@ustadmobile.com"
        const val WHATSAPP_URL = "https://wa.me/"
        const val WHATSAPP_PHONE_NUMBER = "+919828932811"
        const val WEB_URL="https://respect.world/"
        const val EMAIL_RECIPIENT="mandvi2346verma@gmail.com"
        const val GUESS="guess:"
    }
}

