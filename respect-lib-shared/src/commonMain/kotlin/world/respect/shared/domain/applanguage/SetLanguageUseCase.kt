package world.respect.shared.domain.applanguage

interface SetLanguageUseCase {

    data class SetLangResult(
        val waitForRestart: Boolean
    )

    operator fun invoke(
        uiLang: RespectMobileSystemCommon.UiLanguage
    ): SetLangResult

}
