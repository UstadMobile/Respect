package world.respect.clitools

import kotlinx.coroutines.runBlocking
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.opds.model.OpdsFeed
import world.respect.datasource.opds.model.OpdsPublication
import world.respect.datasource.opds.model.ReadiumLink
import world.respect.di.jvmKoinAppModule
import world.respect.domain.validator.ListAndPrintlnValidatorReporter
import world.respect.domain.validator.ValidateLinkUseCase
import world.respect.domain.validator.ValidatorMessage


@Suppress("unused")
class RespectCLI : KoinComponent {

    private val validator: ValidateLinkUseCase by inject()

    fun run(args: Array<String>) {
        val parser = ArgumentParsers.newFor("respect-cli").build()
        val subparsers = parser.addSubparsers()
            .title("subcommands")
            .description("valid subcommands")
            .dest("subparser_name")
            .help("additional help")
            .metavar("COMMAND")

        subparsers.addParser(CMD_VALIDATE).also {
            it.addArgument("-u", "--url")
                .required(true)
                .help("URL to validate")
            it.addArgument("-f", "--nofollow")
                .required(false)
                .setDefault("false")
                .help("Don't follow links")
            it.addArgument("-t", "--type")
                .setDefault("manifest")
                .choices("manifest", "opds-feed", "opds-publication")
                .help("Type of item to validate. Can be a Respect App Manifest, Opds 2.0 Feed, or Opds 2.0 Publication")
            it.addArgument("-o", "--output")
                .choices("error", "warn", "verbose", "debug")
                .setDefault("warn")
                .help("Output verbosity")
            it.addArgument("-i", "--include-respect-opds-checks")
                .choices("true", "false")
                .required(false)
                .setDefault("true")
                .help("Include RESPECT-specific checks on OPDS feeds and publications e.g. to " +
                        "check Manifest can be discovered, lists required resources, etc. Can " +
                        "be set to false to validate only against the OPDS spec, not against " +
                        "the RESPECT requirements.")
        }.help("Validate a RESPECT App Manifest or OPDS Feed of Learning Units")

        val ns: Namespace
        try {
            ns = parser.parseArgs(args)
            val subCommand = ns?.getString("subparser_name")
            when(subCommand) {
                CMD_VALIDATE -> {
                    val url = ns.getString("url")
                    val minLevel = ValidatorMessage.Level.valueOf(
                        (ns.getString("output") ?: "warn").uppercase()
                    )
                    val noFollow = ns.getString("nofollow")?.ifEmpty { null }
                    val validateType = ns.getString("type")
                    val includeRespectSpecificOpdsChecks = ns.getString("include-respect-opds-checks")
                        ?.ifEmpty { null }

                    val reporter = ListAndPrintlnValidatorReporter(
                        filter = {
                            it.level.ordinal >= minLevel.ordinal
                        }
                    )

                    runBlocking {
                        validator(
                            link = ReadiumLink(
                                href = url,
                                type = when (validateType) {
                                    "manifest" -> RespectAppManifest.MIME_TYPE
                                    "opds-feed" -> OpdsFeed.MEDIA_TYPE
                                    "opds-publication" -> OpdsPublication.MEDIA_TYPE
                                    else -> throw IllegalArgumentException("Invalid type: $validateType")
                                },
                            ),
                            options = ValidateLinkUseCase.ValidatorOptions(
                                followLinks = !(noFollow?.toBoolean() ?: false),
                                skipRespectChecks =
                                    !(includeRespectSpecificOpdsChecks?.toBoolean() ?: true),
                            ),
                            refererUrl = url,
                            reporter = reporter,
                            visitedUrls = mutableListOf(),
                        )
                    }

                    val numErrors = reporter.messages.count {
                        it.level == ValidatorMessage.Level.ERROR
                    }
                    println("Errors: $numErrors")

                    if(numErrors > 0) {
                        System.exit(1)
                    }else {
                        System.exit(0)
                    }
                }
            }
        }catch(e : ArgumentParserException) {
            parser.handleError(e)
            System.exit(if(e is HelpScreenException) 0 else 1)
        }
    }

    companion object {

        const val CMD_VALIDATE = "validate"

        /**
         * DO NOT ATTEMPT TO RUN USING THE PLAY BUTTON IN ANDROID STUDIO! Resources will not be
         * found and it will not work.
         *
         * It can be run using as a Gradle task (respect-cli:run --args='..').
         */
        @JvmStatic
        fun main(args: Array<String>) {
            startKoin {
                modules(jvmKoinAppModule)
            }

            RespectCLI().run(args)
        }
    }
}