package world.respect.shared.domain.opds.validator

import com.networknt.schema.InputFormat
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import world.respect.lib.opds.model.OpdsPublication
import world.respect.domain.opds.validator.verifyMimeTypeAndGetBodyAsText
import world.respect.domain.validator.ValidateLinkUseCase
import world.respect.domain.validator.ValidatorMessage
import world.respect.domain.validator.ValidatorReporter

class OpdsPublicationValidator(
    private val httpClient: HttpClient,
    private val json: Json,
    private val validateOpdsPublicationUseCase: ValidateOpdsPublicationUseCase,
): AbstractJsonSchemaValidator(
    schemaUrl = "https://drafts.opds.io/schema/publication.schema.json"
) {

    override suspend fun invoke(
        url: String,
        options: ValidateLinkUseCase.ValidatorOptions,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
        linkValidator: ValidateLinkUseCase?
    ) {
        try {
            val text = httpClient.verifyMimeTypeAndGetBodyAsText(
                url = url,
                acceptableMimeTypes = listOf(OpdsPublication.MEDIA_TYPE, "application/json"),
                reporter = reporter
            )

            val messages = schema.validate(text, InputFormat.JSON)
            messages.forEach {
                reporter.addMessage(it.toValidatorMessage(sourceUri = url))
            }

            val opdsPublication = json.decodeFromString<OpdsPublication>(text)
            val publicationValidation = validateOpdsPublicationUseCase(opdsPublication, url, reporter)

            (opdsPublication.links + publicationValidation.discoveredManifestLinksToValidate).forEach { link ->
                linkValidator?.invoke(
                    link = link,
                    refererUrl = url,
                    options = options,
                    reporter = reporter,
                    visitedUrls = visitedUrls
                )
            }
        }catch (e: Throwable) {
            reporter.addMessage(ValidatorMessage.fromException(url, e))
        }
    }
}