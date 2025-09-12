package world.respect.server

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import kotlin.system.exitProcess

const val CMD_RUN_SERVER = "runserver"

const val CMD_ADD_SCHOOL = "addschool"

fun main(args: Array<String>) {
    val parser = ArgumentParsers.newFor("respect-server").build()

    val subparsers = parser.addSubparsers()
        .title("subcommands")
        .description("valid subcommands")
        .dest("subparser_name")
        .help("additional help")
        .metavar("COMMAND")

    subparsers.addParser(CMD_RUN_SERVER).help("Run RESPECT http server")
    subparsers.addParser(CMD_ADD_SCHOOL).help("Add a new school").also {
        it.addArgument("-u", "--url").help("School URL")
        it.addArgument("-i", "--inviteprefix").help("School invite prefix")
        it.addArgument("-n", "--name").help("School name")
        it.addArgument("-d", "--dburl").help("DB url: path to SQLite file (absolute or relative to school data directory)")
        it.addArgument("-a", "--adminusername").help("Admin username")
        it.addArgument("-p", "--adminpassword")
        it.addArgument("-r", "--rpId").help("Passkey rpId")
            .help("Admin password")
    }

    val ns: Namespace?
    try {
        ns = parser.takeIf {
            args.isNotEmpty() && args.firstOrNull() != CMD_RUN_SERVER
        }?.parseArgs(args)
        val subCommand = ns?.getString("subparser_name") ?: CMD_RUN_SERVER

        when(subCommand) {
            CMD_RUN_SERVER -> {
                //As per https://ktor.io/docs/server-create-a-new-project.html#change-the-port-via-yaml
                io.ktor.server.netty.EngineMain.main(args)
            }
            else -> {
                if(ns == null)
                    throw IllegalStateException("ns should not be null if not running $CMD_RUN_SERVER")

                managerServerMain(ns)
            }
        }
    }catch (e: ArgumentParserException) {
        parser.handleError(e)
        exitProcess(if (e is HelpScreenException) 0 else 1)
    }
}