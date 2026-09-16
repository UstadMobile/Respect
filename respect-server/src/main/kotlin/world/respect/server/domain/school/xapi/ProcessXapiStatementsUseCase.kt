package world.respect.server.domain.school.xapi

import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb.Companion.ID_REPORT_QUERY_REQUEST
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.xapi.asRunReportRequest
import world.respect.shared.domain.xapi.createReportResponseStatement

/**
 * Use case to process incoming xAPI statements and generate response statement
 */
class ProcessXapiStatementsUseCase(
    private val statementResource: XapiStatementsResource,
    private val runReportUseCase: RunReportUseCase,
    private val json: Json,
    private val uidNumberMapper: UidNumberMapper,
    private val accountPersonGuid: String,
) {

    suspend operator fun invoke(statements: List<XapiStatement>) = coroutineScope {
        statements.forEach { statement ->
            if (statement.verb.id == ID_REPORT_QUERY_REQUEST) {
                val request = statement.asRunReportRequest(
                    json = json,
                    accountPersonUid = uidNumberMapper(accountPersonGuid),
                    timeZone = TimeZone.currentSystemDefault()
                )

                try {
                    runReportUseCase(request).collect { result ->
                        val responseStatement = createReportResponseStatement(
                            requestStatement = statement,
                            result = result,
                            json = json
                        )
                        statementResource.post(listOf(responseStatement))
                    }
                } catch (e: Exception) {
                    println("Error running report for request ${request.reportUid}: ${e.message}")
                }
            }
        }
    }
}
