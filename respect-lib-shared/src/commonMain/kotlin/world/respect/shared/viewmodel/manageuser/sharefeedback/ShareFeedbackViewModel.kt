package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.feedback.FeedbackTicket
import world.respect.shared.domain.feedback.Article
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.feedback.CreateTicketUseCase
import world.respect.shared.domain.feedback.FeedbackCategory
import world.respect.shared.domain.feedback.GetFeedbackInfoUseCase
import world.respect.shared.domain.launchers.EmailLauncherUseCase
import world.respect.shared.domain.launchers.WebLauncherUseCase
import world.respect.shared.domain.launchers.WhatsAppLauncherUseCase
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
import world.respect.shared.navigation.ShareFeedback
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

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
    val errorMessage: UiText? = null,
) {
    val hasErrors: Boolean
        get() = feedbackDescriptionError != null ||
                phoneNumberError != null ||
                emailError != null
}

class ShareFeedbackViewModel(
    accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
    private val whatsAppLauncherUseCase: WhatsAppLauncherUseCase,
    private val emailLauncherUseCase: EmailLauncherUseCase,
    private val webLauncherUseCase: WebLauncherUseCase,
    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val feedbackInfoUseCase: GetFeedbackInfoUseCase,
    private val createTicketUseCase: CreateTicketUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(ShareFeedbackUiState())

    val uiState = _uiState.asStateFlow()

    var subject = ""


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.share_feedback.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
        viewModelScope.launch {
            subject = getString(Res.string.feedback_respect)
        }
    }

    fun onClickWhatsApp() {
        viewModelScope.launch {
            whatsAppLauncherUseCase.launchWhatsApp(
                feedbackInfoUseCase().respectPhoneNumber
            )
        }
    }

    fun onClickEmail() {
        viewModelScope.launch {
            emailLauncherUseCase.sendEmail(
                feedbackInfoUseCase().respectEmailId,
                subject
            )
        }
    }

    fun onClickPublicForum() {
        viewModelScope.launch {
            webLauncherUseCase.launchWeb()
        }
    }

    fun onCategorySelected(category: FeedbackCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category
        )
    }

    fun onFeedbackDescriptionChanged(text: String) {
        _uiState.update {
            it.copy(
                feedbackDescription = text,
                feedbackDescriptionError = null
            )
        }
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

        _uiState.update {
            it.copy(errorMessage = null)
        }

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
                if (_uiState.value.phoneNumber.isNotBlank() && _uiState.value.nationalPhoneNumSet
                    && !phoneNumValidatorUseCase.isValid(_uiState.value.phoneNumber)
                ) {
                    phoneNumberError = Res.string.invalid.asUiText()
                }
                if (_uiState.value.email.isNotBlank() &&
                    !validateEmailUseCase(_uiState.value.email)
                ) {
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
                    feedbackInfoUseCase().respectEmailId
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

                val response=createTicketUseCase(ticket)

                loadingState = LoadingUiState.NOT_LOADING

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = FeedbackSubmitted(response.id),
                        popUpToClass = ShareFeedback::class,
                        popUpToInclusive = true
                    )
                )
            } catch (_: Exception) {
                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update {
                    it.copy(errorMessage = Res.string.error_message.asUiText())
                }
            }
        }
    }

    companion object {
        const val DEFAULT_CUSTOMER_ENDPOINT = "@ustadmobile.com"
        const val WHATSAPP_URL = "https://wa.me/"
        const val WEB_URL = "https://respect.world/"
        const val GUESS = "guess:"
        const val DEFAULT_GROUP_ID = "1"
    }
}

