package world.respect.clitools

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import world.respect.domain.opds.validator.OpdsValidatorUseCase

@Suppress("unused")
class RespectCLI {

    companion object {

        const val CMD_VALIDATE_OPDS = "validate-opds"

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgumentParsers.newFor("respect-cli").build()
            val subparsers = parser.addSubparsers()
                .title("subcommands")
                .description("valid subcommands: ")
                .dest("subparser_name")
                .help("additional help")
                .metavar("COMMAND")

            subparsers.addParser(CMD_VALIDATE_OPDS).also {
                it.addArgument("-u", "--url")
                    .required(true)
                    .help("OPDS feed URL")
                it.addArgument("-r", "--recursive")
                    .required(false)
                    .setDefault("true")
                    .help("Validate all linked feeds")
            }.help("Validate an OPDS feed")

            val ns: Namespace
            try {
                ns = parser.parseArgs(args)
                val subCommand = ns?.getString("subparser_name")
                when(subCommand) {
                    CMD_VALIDATE_OPDS -> {
                        val url = ns.getString("url")
                        val recursive = ns.getString("recursive")
                        println("Validating $url ...")
                        val messages= OpdsValidatorUseCase().invoke(
                            url = url,
                            recursive = recursive.toBoolean(),
                            visitedFeeds = mutableListOf()
                        )

                        val numErrors = messages.count { it.isError }
                        println("Errors: $numErrors")
                        messages.forEach {
                            println(it.message)
                        }

                        if(numErrors > 0) {
                            System.exit(1)
                        }
                    }
                }
            }catch(e : ArgumentParserException) {
                parser.handleError(e)
                System.exit(if(e is HelpScreenException) 0 else 1)
            }
        }
    }
}