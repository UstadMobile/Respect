package world.respect.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import world.respect.lib.opds.model.name
import world.respect.shared.domain.contentfilter.ContentFilterOption
import world.respect.shared.domain.contentfilter.ContentFilterOptions
import world.respect.shared.domain.contentfilter.ContentFilterType
import world.respect.shared.domain.getlanguageendonym.GetLanguageEndonymUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.subject
import world.respect.shared.generated.resources.type

/**
 * Build the content filter chips.
 *
 * Language: options from publication metadata.language, displayed as endonyms using
 * GetLanguageEndonymUseCase (as per AppDetail and PublicationDetail).
 * Subject: options from publication metadata.subject.
 * Type: hardcoded options (see [ContentFilterType]); always shown when [includeType] is true.
 *
 * Language and subject are omitted when there are no options available.
 */
@Composable
fun contentFilterDefinitions(
    options: ContentFilterOptions,
    includeType: Boolean,
): List<RespectFilterDefinition<ContentFilterOption>> {
    val koin = getKoin()
    val getLanguageEndonymUseCase: GetLanguageEndonymUseCase = remember { koin.get() }

    val languageOptions = options.languages.map { langCode ->
        RespectFilterOption<ContentFilterOption>(
            value = ContentFilterOption.Language(langCode),
            label = getLanguageEndonymUseCase(langCode)?.let { stringResource(it) } ?: langCode,
        )
    }.sortedBy { it.label }

    val subjectOptions = options.subjects.map { subject ->
        RespectFilterOption<ContentFilterOption>(
            value = ContentFilterOption.Subject(subject),
            label = langMapString(subject.name),
        )
    }.sortedBy { it.label }

    val typeOptions = if (includeType) {
        ContentFilterType.entries.map { type ->
            RespectFilterOption<ContentFilterOption>(
                value = ContentFilterOption.Type(type),
                label = stringResource(type.label),
            )
        }
    } else {
        emptyList()
    }

    return listOfNotNull(
        RespectFilterDefinition(
            id = FILTER_ID_LANGUAGE,
            label = stringResource(Res.string.language),
            options = languageOptions,
        ).takeIf { languageOptions.isNotEmpty() },
        RespectFilterDefinition(
            id = FILTER_ID_SUBJECT,
            label = stringResource(Res.string.subject),
            options = subjectOptions,
        ).takeIf { subjectOptions.isNotEmpty() },
        RespectFilterDefinition(
            id = FILTER_ID_TYPE,
            label = stringResource(Res.string.type),
            options = typeOptions,
        ).takeIf { includeType },
    )
}

private const val FILTER_ID_LANGUAGE = "language"
private const val FILTER_ID_SUBJECT = "subject"
private const val FILTER_ID_TYPE = "type"
