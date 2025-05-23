package world.respect

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException
import world.respect.domain.opds.validator.OpdsValidator
import java.net.URI
import kotlin.system.exitProcess

/**
 * Command line application for validating OPDS feeds and publications.
 * This application accepts a URI to an OPDS feed or publication and validates
 * it according to the OPDS 2.0 specification.
 *
 * For reference, see the schemas:
 * - https://drafts.opds.io/schema/feed.schema.json
 * - https://drafts.opds.io/schema/publication.schema.json
 */
object OpdsCliApp {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create the argument parser
        val parser = ArgumentParsers.newFor("opds-validator").build()
            .defaultHelp(true)
            .description("Validates OPDS 2.0 feeds and publications against the specification")

        // Required URI argument
        parser.addArgument("uri")
            .help("URI to the OPDS feed or publication (file:// or http(s)://)")
            .nargs("?") // Make it optional so we can show help if not provided

        // Optional flags
        parser.addArgument("--no-exit-code")
            .action(Arguments.storeTrue())
            .help("Do not exit with error code when validation fails")

        try {
            // Parse the arguments
            val namespace = parser.parseArgs(args)

            // Get the URI argument
            val uriArg = namespace.getString("uri")
            if (uriArg == null) {
                parser.printHelp()
                return
            }

            // Get optional flags
            val noExitCode: Boolean = namespace.getBoolean("no-exit-code") ?: false
            try {
                val uri = URI(uriArg)
                println("Attempting to access URI: $uri")
                println("URI scheme: ${uri.scheme}")

                // Create validator instance
                val validator = OpdsValidator()

                // Start validation
                println("Starting validation...")
                println("Following links during validation...")

                // Validate the URI with the validator - always follow links
                val result = validator.validateOpdsUri(uri, followLinks = true)

                // Process validation result
                when (result) {
                    is OpdsValidator.ValidationResult.Success -> {
                        val feedType = when (result.feedType) {
                            OpdsValidator.FeedType.FEED -> "OPDS feed"
                            OpdsValidator.FeedType.PUBLICATION -> "OPDS publication"
                        }

                        println("Validation successful: The file is a valid $feedType.")

                        if (result.warnings.isNotEmpty()) {
                            println("\nWarnings:")
                            result.warnings.forEach { println("- $it") }
                        }
                    }

                    is OpdsValidator.ValidationResult.Error -> {
                        println("Validation failed.")
                        println("Errors:")
                        result.errors.forEach { println("- $it") }

                        // Only exit with error code if not running in non-exit mode
                        if (!noExitCode) {
                            exitProcess(1)
                        }
                    }

                    is OpdsValidator.ValidationResult.Exception -> {
                        println("Error: ${result.message}")
                        println("Details: ${result.exception.message}")

                        // Only exit with error code if not running in non-exit mode
                        if (!noExitCode) {
                            exitProcess(2)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()

                // Only exit with error code if not running in non-exit mode
                if (!noExitCode) {
                    exitProcess(1)
                }
            }
        } catch (e: ArgumentParserException) {
            // Handle argument parsing exceptions and show help
            parser.handleError(e)
            exitProcess(1)
        }
    }
}