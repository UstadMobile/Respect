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
import world.respect.datalayer.sharefeedback.FeedBackDataSource
import world.respect.datalayer.sharefeedback.model.FeedbackTicket
import world.respect.datalayer.sharefeedback.FeedBackDataSource.Companion.DEFAULT_GROUP_ID
import world.respect.datalayer.sharefeedback.model.Article
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.feedback.FeedbackCategory
import world.respect.shared.domain.launchers.EmailLauncher
import world.respect.shared.domain.launchers.WebLauncher
import world.respect.shared.domain.launchers.WhatsAppLauncher
import world.respect.shared.domain.phonenumber.PhoneNumValidatorUseCase
import world.respect.shared.domain.validateemail.ValidateEmailUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.feedback_respect
import world.respect.shared.generated.resources.phone_number
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.share_feedback
import world.respect.shared.generated.resources.enter_one_field
import world.respect.shared.generated.resources.error_message
import world.respect.shared.generated.resources.invalid
import world.respect.shared.generated.resources.invalid_email
import world.respect.shared.navigation.FeedbackSubmitted
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.LoadingUiState
import kotlin.getValue

data class ShareFeedbackUiState(
    val categories: List<FeedbackCategory> = FeedbackCategory.entries,
    val selectedCategory: FeedbackCategory = FeedbackCategory.LAUNCHER,
    val feedbackDescription: String = "",
    val isCheckBoxSelected: Boolean = false,
    val phoneNumber: String = "",
    val email: String = "",
    val nationalPhoneNumSet: Boolean = false,
    val feedbackDescriptionError: UiText? = null,
    val phoneNumberError: UiText? = null,
    val emailError: UiText? = null,
    val errorMessage: String? = null,
) {
    val hasErrors: Boolean
        get() = feedbackDescriptionError != null ||
                phoneNumberError != null ||
                emailError != null
}

class ShareFeedbackViewModel(
    accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val emailLauncher: EmailLauncher,
    private val webLauncher: WebLauncher,
    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(ShareFeedbackUiState())

    val uiState = _uiState.asStateFlow()

    var subject = ""

    private val feedBackDataSource: FeedBackDataSource by inject()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.share_feedback.asUiText(),
            )
        }
        viewModelScope.launch {
            subject = getString(Res.string.feedback_respect)
        }
    }

    fun onFeedbackDescriptionChanged(text: String) {
        _uiState.update {
            it.copy(
                feedbackDescription = text,
                feedbackDescriptionError = null
            )
        }
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

    fun onCategorySelected(category: FeedbackCategory) {
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
        _uiState.update {
            val isRequiredError = it.emailError == Res.string.enter_one_field.asUiText()
            it.copy(
                phoneNumber = phone,
                phoneNumberError = null,
                emailError = if (isRequiredError) null else it.emailError
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update {
            val isRequiredError = it.emailError == Res.string.enter_one_field.asUiText()
            it.copy(
                email = email,
                emailError = null,
                phoneNumberError = if (isRequiredError) null else it.phoneNumberError
            )
        }
    }

    fun onClickSubmit() {
        val feedbackDescriptionError =
            if (_uiState.value.feedbackDescription.isBlank()) {
                Res.string.required_field.asUiText()
            } else null

        var phoneNumberError: UiText? = null
        var emailError: UiText? = null

        if (_uiState.value.isCheckBoxSelected) {
            if (_uiState.value.phoneNumber.isBlank() && _uiState.value.email.isBlank()) {
                phoneNumberError = "".asUiText()
                emailError = Res.string.enter_one_field.asUiText()
            } else {
                if (_uiState.value.phoneNumber.isNotBlank() && _uiState.value.nationalPhoneNumSet &&
                    !phoneNumValidatorUseCase.isValid(_uiState.value.phoneNumber)
                ) {
                    phoneNumberError = Res.string.invalid.asUiText()
                }
                if (_uiState.value.email.isNotBlank() && !validateEmailUseCase(_uiState.value.email)) {
                    emailError = Res.string.invalid_email.asUiText()
                }
            }
        }

        _uiState.update {
            it.copy(
                feedbackDescriptionError = feedbackDescriptionError,
                phoneNumberError = phoneNumberError,
                emailError = emailError
            )
        }
        if (_uiState.value.hasErrors)
            return

        viewModelScope.launch {
            try {
                loadingState = LoadingUiState.INDETERMINATE
                val customerEmail = if (_uiState.value.isCheckBoxSelected) {
                    _uiState.value.email.ifBlank {
                        "${_uiState.value.phoneNumber}$DEFAULT_CUSTOMER_ENDPOINT"
                    }
                } else {
                    DEFAULT_CUSTOMER_ID
                }

                val ticket = FeedbackTicket(
                    title = getString(_uiState.value.selectedCategory.resource),
                    groupId = DEFAULT_GROUP_ID,
                    customerId = "$GUESS$customerEmail",
                    article = Article(
                        subject = subject,
                        body = "${_uiState.value.feedbackDescription}\n\n" +
                                "${getString(Res.string.phone_number)}: " +
                                _uiState.value.phoneNumber,
                    )
                )

                feedBackDataSource.createTicket(ticket)

                loadingState = LoadingUiState.NOT_LOADING

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        FeedbackSubmitted
                    )
                )
            } catch (e: Exception) {
                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update {
                    it.copy( errorMessage = getString(Res.string.error_message))
                }
            }
        }
    }

    companion object {
        const val DEFAULT_CUSTOMER_ENDPOINT = "@ustadmobile.com"
        const val DEFAULT_CUSTOMER_ID = "info@ustadmobile.com"
        const val WHATSAPP_URL = "https://wa.me/"
        const val WHATSAPP_PHONE_NUMBER = "+919828932811"
        const val WEB_URL = "https://respect.world/"
        const val EMAIL_RECIPIENT = "respect.app.tester2026@gmail.com"
        const val GUESS = "guess:"
    }
}

