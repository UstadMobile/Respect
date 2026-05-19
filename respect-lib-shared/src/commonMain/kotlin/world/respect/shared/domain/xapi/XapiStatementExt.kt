package world.respect.shared.domain.xapi

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.Person
import world.respect.lib.xapi.model.VERB_VOIDED
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

/**
 * Extension function to convert a Person to an XapiAgent with the school's home page.
 *
 * @param schoolSelfUrl The URL of the school to use as the account home page
 * @return An XapiAgent representing this person
 */
fun Person.toXapiAgent(schoolSelfUrl: String): XapiAgent {
    return XapiAgent(
        name = fullName(),
        objectType = XapiObjectType.Agent,
        account = XapiAccount(
            name = guid,
            homePage = schoolSelfUrl
        )
    )
}

/**
 * Create an XapiAgent for querying purposes using an identifier and school URL.
 *
 * @param identifier The identifier (e.g., group ID) to use as the account name
 * @param schoolSelfUrl The URL of the school to use as the account home page
 * @return An XapiAgent for querying
 */
fun createXapiAgentForQuery(identifier: String, schoolSelfUrl: String): XapiAgent {
    return XapiAgent(
        objectType = XapiObjectType.Agent,
        account = XapiAccount(
            name = identifier,
            homePage = schoolSelfUrl
        )
    )
}

/**
 * Create a voiding statement for an existing xAPI statement.
 *
 * @param actor The actor performing the voiding action
 * @param statementId The ID of the statement to void
 * @return An XapiStatement that voids the specified statement
 */
@OptIn(ExperimentalUuidApi::class)
fun createVoidingStatement(actor: XapiAgent, statementId: String): XapiStatement {
    return XapiStatement(
        actor = actor,
        verb = XapiVerb(id = VERB_VOIDED),
        `object` = XapiStatementRef(
            objectType = XapiObjectType.StatementRef,
            id = statementId
        ),
        timestamp = Clock.System.now()
    )
}



