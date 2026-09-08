package org.openeel.libxapi.test

import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementFormatEnum
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.uuid.Uuid


/**
 * Asserts that the given [statement] can be successfully stored in the provided [resource]
 * and subsequently retrieved across different statement formats ([GetStatementFormatEnum.CANONICAL],
 * [GetStatementFormatEnum.EXACT], and [GetStatementFormatEnum.IDS]).
 *
 * Verifies that:
 * - The statement can be posted to the [resource].
 * - Retrieving with [GetStatementFormatEnum.CANONICAL] returns a statement canonically equal to [statement].
 * - Retrieving with [GetStatementFormatEnum.EXACT] returns a statement exactly equal to [statement].
 * - Retrieving with [GetStatementFormatEnum.IDS] returns a statement canonically equal to [statement] in ID-only format.
 *
 * @param statement The [XapiStatement] to store and verify. Must have a non-null [XapiStatement.id].
 * @param resource The [XapiStatementsResource] used to store and retrieve the statement.
 */
suspend fun assertStatementCanBeStoredAndRetrieved(
    statement: XapiStatement,
    resource: XapiStatementsResource
) {
    val stmtUuid = statement.id
        ?: throw IllegalArgumentException("Statement id to be stored must not be null")

    resource.post(listOf(statement))

    //check canonical match
    val canonicalStmtFromDb = resource.get(

        listParams = XapiStatementsResource.GetStatementParams(
            format = GetStatementFormatEnum.CANONICAL,
            statementId = stmtUuid
        )
    ).dataOrNull()?.statements?.first()

    assertNotNull(canonicalStmtFromDb)
    assertXapiStatementCanonicallyEqual(
        expected = statement,
        actual = canonicalStmtFromDb,
    )

    val exactStmtFromDb = resource.get(

        listParams = XapiStatementsResource.GetStatementParams(
            format = GetStatementFormatEnum.EXACT,
            statementId = stmtUuid
        )
    ).dataOrNull()?.statements?.first()
    assertEquals(statement, exactStmtFromDb)

    val idOnlyStmtFromDb = resource.get(
        listParams = XapiStatementsResource.GetStatementParams(
            format = GetStatementFormatEnum.IDS,
            statementId = stmtUuid
        )
    ).dataOrNull()?.statements?.first()
    assertNotNull(idOnlyStmtFromDb)
    assertXapiStatementCanonicallyEqual(
        expected = statement,
        actual = idOnlyStmtFromDb,
        idOnlyFormat = true,
    )
}

suspend fun assertCanVoidStatement(
    statement: XapiStatement,
    resource: XapiStatementsResource,
) {
    val stmtUuid = statement.id ?: throw IllegalArgumentException("Statement id to be stored must not be null")

    val getStmtParams = XapiStatementsResource.GetStatementParams(
        statementId = stmtUuid
    )

    resource.post(listOf(statement))

    assertXapiStatementCanonicallyEqual(
        expected = statement,
        actual = resource.get(
            listParams = getStmtParams
        ).dataOrNull()?.statements?.firstOrNull()!!
    )

    val voidingStatement = XapiStatement(
        actor = statement.actor,
        verb = XapiVerb(id = XapiVerb.ID_VOIDED),
        `object` = XapiStatementRef(id = stmtUuid.toString())
    )
    resource.post(listOf(voidingStatement))

    GetStatementFormatEnum.entries.forEach { format ->
        assertNull(
            resource.get(
                listParams = getStmtParams.copy(
                    format = format
                )
            ).dataOrNull()?.statements?.firstOrNull()
        )
    }

    val getByVoidedParams = XapiStatementsResource.GetStatementParams(
        voidedStatementId = stmtUuid
    )
    assertEquals(
        expected = statement,
        actual = resource.get(
            getByVoidedParams.copy(format = GetStatementFormatEnum.EXACT)
        ).dataOrNull()?.statements?.firstOrNull()
    )
    assertXapiStatementCanonicallyEqual(
        expected = statement,
        actual = resource.get(
            getByVoidedParams.copy(format = GetStatementFormatEnum.CANONICAL)
        ).dataOrNull()?.statements?.firstOrNull()!!
    )
}

suspend fun assertDuplicateStatementWillThrowConflictError(
    statement: XapiStatement,
    resource: XapiStatementsResource,
) {
    resource.post(listOf(statement))
    try {
        resource.post(listOf(statement))

        throw IllegalStateException("Should not get here")
    }catch(e: XapiException) {
        assertEquals(409, e.httpStatusCode)
    }
}

