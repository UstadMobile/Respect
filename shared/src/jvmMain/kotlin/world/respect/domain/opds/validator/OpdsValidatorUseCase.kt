package world.respect.domain.opds.validator

import com.networknt.schema.InputFormat
import world.respect.domain.validator.ValidatorMessage
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import kotlinx.serialization.json.Json
import world.respect.domain.opds.model.OpdsFeed
import world.respect.domain.opds.model.OpdsLink
import java.net.URI

class OpdsValidatorUseCase(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {

    private val factory by lazy {
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
    }

    private val schema by lazy {
        factory.getSchema(URI("https://drafts.opds.io/schema/feed.schema.json")).also {
            it.initializeValidators()
        }
    }

    operator fun invoke(
        url: String,
        recursive: Boolean,
        visitedFeeds: MutableList<String>,
    ): List<ValidatorMessage> {
        val validationMessages = mutableListOf<ValidatorMessage>()

        try {
            val urlUri = URI(url)
            val text = urlUri.toURL().readText()
            visitedFeeds.add(url)

            val messages = schema.validate(text, InputFormat.JSON)
            validationMessages.addAll(
                messages.map {
                    ValidatorMessage(
                        isError = true,
                        sourceUri = url,
                        message = it.toString()
                    )
                }
            )

            val opdsFeed = json.decodeFromString<OpdsFeed>(text)

            if(recursive) {
                val linksToValidate = buildList<OpdsLink> {
                    fun List<OpdsLink>?.addAllOpdsFeeds() {
                        this?.filter { it.type == OPDS_JSON_MIME_TYPE }?.also { addAll(it) }
                    }

                    opdsFeed.navigation?.addAllOpdsFeeds()
                    opdsFeed.facets?.flatMap { it.links }?.addAllOpdsFeeds()
                    opdsFeed.groups?.flatMap { it.links ?: emptyList() }?.addAllOpdsFeeds()
                    opdsFeed.publications?.flatMap { it.links }?.addAllOpdsFeeds()
                }

                linksToValidate.forEach { link ->
                    val linkUrl = urlUri.resolve(link.href).toURL().toString()
                    if(!visitedFeeds.contains(linkUrl)) {
                        validationMessages += invoke(
                            url = linkUrl,
                            recursive = recursive,
                            visitedFeeds = visitedFeeds,
                        )
                    }
                }
            }


        }catch(e : Throwable) {
            validationMessages += ValidatorMessage(
                isError = true,
                sourceUri = url,
                message = "Error processing $url : ${e.message}"
            )
        }

        return validationMessages
    }

    companion object {

        const val OPDS_JSON_MIME_TYPE = "application/opds+json"

    }

}