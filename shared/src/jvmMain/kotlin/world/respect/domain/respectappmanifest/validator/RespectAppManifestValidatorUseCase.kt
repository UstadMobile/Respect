package world.respect.domain.respectappmanifest.validator

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.domain.getfavicons.GetFavIconUseCase
import world.respect.domain.licenses.model.SpdxLicenseList
import world.respect.domain.opds.model.OpdsFeed
import world.respect.domain.opds.model.ReadiumLink
import world.respect.domain.opds.model.toStringMap
import world.respect.domain.respectdir.model.RespectAppManifest
import world.respect.domain.validator.ValidateHttpResponseForUrlUseCase
import world.respect.domain.validator.ValidatorMessage
import world.respect.domain.validator.ValidatorReporter
import world.respect.domain.validator.ValidatorUseCase
import java.net.URI

class RespectAppManifestValidatorUseCase(
    private val json: Json,
    private val validateHttpResponseForUrlUseCase: ValidateHttpResponseForUrlUseCase,
    private val getFavIconUseCase: GetFavIconUseCase,
) : ValidatorUseCase {

    /**
     * Validate the a RespectAppManifest as per the KDoc
     *
     * @param link Link that points to a JSON for a RespectAppManifest
     */
    override suspend operator fun invoke(
        link: ReadiumLink,
        baseUrl: String,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
        followLinks: Boolean,
    ) {
        val absoluteUrl = URI(baseUrl).resolve(link.href).toURL()

        try {
            val text = absoluteUrl.readText()
            val respectAppManifest: RespectAppManifest = json.decodeFromString(text)

            respectAppManifest.name.toStringMap().forEach { (_, value) ->
                if(value.isBlank() || value.length > TITLE_MAX_CHARS) {
                    reporter.addMessage(ValidatorMessage(true, absoluteUrl.toString(),
                        "title \"$value\" invalid length: not between 1 and $TITLE_MAX_CHARS chars"))
                }
            }

            respectAppManifest.description?.toStringMap()?.forEach { (_, value) ->
                if(value.isBlank() || value.length > DESCRIPTION_MAX_CHARS) {
                    reporter.addMessage(ValidatorMessage(true, absoluteUrl.toString(),
                        "description \"$value\" invalid length: not between 1 and $DESCRIPTION_MAX_CHARS chars"))
                }
            }

            val license = respectAppManifest.license
            val allLicenses: SpdxLicenseList = json.decodeFromString(
                this::class.java.getResourceAsStream(
                    "/world/respect/domain/validator/licenses.json"
                )!!.bufferedReader().readText()
            )

            if(license != LICENSE_PROPRIETARY && !allLicenses.licenses.any { it.licenseId == license }) {
                reporter.addMessage(ValidatorMessage(
                    true, absoluteUrl.toString(), "Invalid license: $license"
                ))
            }

            val websiteVal = respectAppManifest.website
            if(websiteVal != null) {
                validateHttpResponseForUrlUseCase(websiteVal.toString(), reporter)
            }else {
                reporter.addMessage(ValidatorMessage(true, absoluteUrl.toString(),"website is required"))
            }

            val icon = respectAppManifest.icon ?: getFavIconUseCase(
                Url(absoluteUrl.toString())
            ).firstOrNull {
                it.type in ACCEPTABLE_ICON_FORMATS && ((it.width ?: 0) >= ICON_REQUIRED_SIZE) &&
                        ((it.height ?: 0) >= ICON_REQUIRED_SIZE)
            }

            if(icon == null) {
                reporter.addMessage(ValidatorMessage(true, absoluteUrl.toString(),
                    "No acceptable icon (webp or png) with resolution >= 512 pixels found. " +
                            "If website does not have an acceptable favicon, must be explicitly specified"))
            }

            validateHttpResponseForUrlUseCase(
                url = respectAppManifest.learningUnits.toString(),
                reporter = reporter,
                options = ValidateHttpResponseForUrlUseCase.ValidationOptions(
                    acceptableMimeTypes = listOf("application/json", OpdsFeed.MEDIA_TYPE)
                )
            )

            respectAppManifest.android?.packageId?.also { packageId ->
                if(packageId.any { it !in PACKAGE_ID_ALLOWED_CHARS }) {

                    reporter.addMessage(
                        ValidatorMessage(true, absoluteUrl.toString(), "Invalid packageId: $packageId")
                    )
                }
            }
        }catch(e: Throwable) {
            reporter.addMessage(ValidatorMessage.fromException(absoluteUrl.toString(), e))
        }
    }


    companion object  {

        val PACKAGE_ID_ALLOWED_CHARS = ('a' .. 'z').plus('A'..'Z')
            .plus('0'..'9').plus("._$".asIterable())

        val ACCEPTABLE_ICON_FORMATS = listOf("image/png", "image/webp")

        const val ICON_REQUIRED_SIZE = 512

        const val TITLE_MAX_CHARS = 80

        const val DESCRIPTION_MAX_CHARS = 4000

        const val LICENSE_PROPRIETARY = "proprietary"



    }

}