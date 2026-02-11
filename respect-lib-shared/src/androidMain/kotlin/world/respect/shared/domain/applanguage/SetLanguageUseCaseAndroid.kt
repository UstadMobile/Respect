package world.respect.shared.domain.applanguage

class SetLanguageUseCaseAndroid(
    private val languagesConfig: SupportedLanguagesConfig,
): SetLanguageUseCase {

    override fun invoke(
        uiLang: RespectMobileSystemCommon.UiLanguage,
    ): SetLanguageUseCase.SetLangResult {

        languagesConfig.localeSetting = uiLang.langCode

        return SetLanguageUseCase.SetLangResult(
            waitForRestart = true
        )
    }
}