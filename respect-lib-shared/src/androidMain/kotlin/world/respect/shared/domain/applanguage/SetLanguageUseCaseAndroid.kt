package world.respect.shared.domain.applanguage

class SetLanguageUseCaseAndroid(
    private val languagesConfig: SupportedLanguagesConfig,
): SetLanguageUseCase {

    override fun invoke(
        uiLang: RespectMobileSystemCommon.UiLanguage,
    ): SetLanguageUseCase.SetLangResult {
        //languagesConfig uses a delegate on Android that will use Android's per-app locale setting
        languagesConfig.localeSetting = uiLang.langCode

        return SetLanguageUseCase.SetLangResult(
            waitForRestart = true
        )
    }
}