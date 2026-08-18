package world.respect.shared.domain.report.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.COLUMN_NAME_SUBGROUP
import world.respect.shared.domain.xapi.COLUMN_NAME_X_AXIS
import world.respect.shared.domain.xapi.COLUMN_NAME_Y_AXIS
import world.respect.shared.domain.xapi.toStatementReportRows
import kotlin.time.Clock
import kotlin.uuid.Uuid

class MockRunReportUseCaseClientImpl(
    private val accountManager: RespectAccountManager,
) : RunReportUseCase {

    override fun invoke(
        request: RunReportUseCase.RunReportRequest
    ): Flow<RunReportUseCase.RunReportResult> = flow {

        // 1. Build dummy query-result JSON — simulates server response data
        val queryResultJson = buildJsonObject {
            put("columnNames", buildJsonArray {
                // Shuffle column order to verify that toStatementReportRows discovers indices programmatically
                add(COLUMN_NAME_SUBGROUP)
                add(COLUMN_NAME_X_AXIS)
                add(COLUMN_NAME_Y_AXIS)
            })
            put("rows", buildJsonArray {
                add(buildJsonArray { add(JsonPrimitive("male")); add(JsonPrimitive("2025-01-01")); add(JsonPrimitive(120383.0)) })
                add(buildJsonArray { add(JsonPrimitive("female")); add(JsonPrimitive("2025-01-01")); add(JsonPrimitive(2187324.00)) })
                add(buildJsonArray { add(JsonPrimitive("male")); add(JsonPrimitive("2025-01-02")); add(JsonPrimitive(1223220.0)) })
                add(buildJsonArray { add(JsonPrimitive("female")); add(JsonPrimitive("2025-01-02")); add(JsonPrimitive(922220.0)) })
            })
        }

        // 2. Build the response statement — matches Appendix B format exactly
        val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent ?: return@flow
        val responseStatement = XapiStatement(
            id = Uuid.random(),
            actor = actor,
            verb = XapiVerb(id = XapiVerb.ID_REPORT_QUERY_RESPONSE),
            `object` = XapiStatementRef(
                id = request.reportUid.toString(),
                objectType = XapiObjectType.StatementRef
            ),
            result = XapiResult(
                extensions = mapOf(
                    OpenEelXapiConstants.EXTENSION_REPORT_QUERY_RESULT to queryResultJson
                )
            ),
            timestamp = Clock.System.now(),
            version = "1.0.0"
        )

        // 3. Map to StatementReportRow discovery indices from column names
        val resultRows = responseStatement.toStatementReportRows()

        // 4. Emit result — one list per series (mocking same data for all series)
        emit(
            RunReportUseCase.RunReportResult(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                request = request,
                results = request.reportOptions.series.map { resultRows },
                age = 0
            )
        )
    }
}
