package world.respect.shared.domain.xapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.doubleOrNull
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.OpenEelXapiConstants.CATEGORY_REPORT_QUERY_RECIPE
import world.respect.lib.xapi.ext.decodeFromExtensionOrNull
import world.respect.lib.xapi.ext.encodeWithExtension
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.extensions.queryresponse.XapiSqlQueryResponse
import world.respect.lib.xapi.extensions.reportoptions.ReportOptions
import world.respect.lib.xapi.extensions.reportoptions.StatementReportRow
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiActivityDefinition.Companion.TYPE_REPORT
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.model.XapiVerb.Companion.ID_REPORT_QUERY_REQUEST
import world.respect.lib.xapi.model.XapiVerb.Companion.ID_REPORT_QUERY_RESPONSE
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val COLUMN_NAME_X_AXIS = "xAxis"
const val COLUMN_NAME_Y_AXIS = "yAxis"
const val COLUMN_NAME_SUBGROUP = "subgroup"


@OptIn(ExperimentalUuidApi::class)
fun createBlankReportStatement(
    reportActivityId: String,
    actor: XapiActor,
    reportOptions: ReportOptions,
    json: Json,
    reportDescription: Map<String, String> = emptyMap(),
    query: String = "",
): XapiStatement {
    val now = Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = actor,
        verb = XapiVerb(
            id = ID_REPORT_QUERY_REQUEST,
        ),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = reportActivityId,
            definition = XapiActivityDefinition(
                type = TYPE_REPORT,
                description = reportDescription,
            ).encodeWithExtension(
                json = json,
                extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_QUERY,
                serializer = kotlinx.serialization.serializer(),
                value = query
            ).encodeWithExtension(
                json = json,
                extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
                serializer = ReportOptions.serializer(),
                value = reportOptions
            )
        ),
        context = XapiContext(
            instructor = actor,
            contextActivities = XapiContextActivities(
                category = listOf(
                    XapiActivity(
                        id = CATEGORY_REPORT_QUERY_RECIPE,
                        objectType = XapiObjectType.Activity
                    )
                ),
                grouping = emptyList()
            )
        ),
        timestamp = now,
        version = "1.0.0"
    )
}

fun XapiStatement.asRunReportRequest(
    json: Json,
    accountPersonUid: Long,
    timeZone: kotlinx.datetime.TimeZone
): RunReportUseCase.RunReportRequest {
    val activity = objectActivityOrNull() ?: throw IllegalArgumentException("Statement object is not an activity")
    val reportOptions = activity.definition?.decodeFromExtensionOrNull(
        json = json,
        extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
        deserializer = ReportOptions.serializer()
    ) ?: ReportOptions()

    return RunReportUseCase.RunReportRequest(
        reportUid = activity.id.substringAfterLast("/").toLongOrNull() ?: 0L,
        reportOptions = reportOptions,
        accountPersonUid = accountPersonUid,
        timeZone = timeZone,
    )
}

@OptIn(ExperimentalUuidApi::class)
fun createReportResponseStatement(
    requestStatement: XapiStatement,
    result: RunReportUseCase.RunReportResult,
    json: Json,
): XapiStatement {
    val requestId = requestStatement.id ?: throw IllegalArgumentException("Request statement must have an ID")

    // Map RunReportResult to XapiSqlQueryResponse
    val queryResponses = result.results.map { rows ->
        XapiSqlQueryResponse(
            columnNames = listOf(COLUMN_NAME_X_AXIS, COLUMN_NAME_Y_AXIS, COLUMN_NAME_SUBGROUP),
            rows = JsonArray(rows.map { row ->
                buildJsonArray {
                    add(JsonPrimitive(row.xAxis))
                    add(JsonPrimitive(row.yAxis))
                    add(JsonPrimitive(row.subgroup))
                }
            })
        )
    }

    return XapiStatement(
        id = Uuid.random(),
        actor = requestStatement.actor,
        verb = XapiVerb(id = ID_REPORT_QUERY_RESPONSE),
        `object` = XapiStatementRef(id = requestId.toString()),
        result = XapiResult(
            extensions = mapOf(
                OpenEelXapiConstants.EXTENSION_REPORT_QUERY_RESULT to json.encodeToJsonElement(
                    kotlinx.serialization.builtins.ListSerializer(XapiSqlQueryResponse.serializer()),
                    queryResponses
                )
            )
        ),
        timestamp = Clock.System.now(),
        version = "1.0.0"
    )
}

fun XapiStatement.getQueryResultExtension(): JsonArray? {
    return result?.extensions?.get(OpenEelXapiConstants.EXTENSION_REPORT_QUERY_RESULT) as? JsonArray
}

fun XapiStatement.getQueryResultResponses(json: Json): List<XapiSqlQueryResponse> {
    return getQueryResultExtension()?.mapNotNull {
        try {
            json.decodeFromJsonElement(XapiSqlQueryResponse.serializer(), it)
        } catch (_: Exception) {
            null
        }
    } ?: emptyList()
}

fun XapiStatement.toStatementReportRows(
    seriesIndex: Int,
    json: Json,
    xAxisColumnName: String = COLUMN_NAME_X_AXIS,
    yAxisColumnName: String = COLUMN_NAME_Y_AXIS,
    subgroupColumnName: String = COLUMN_NAME_SUBGROUP
): List<StatementReportRow> {
    val response = getQueryResultResponses(json).getOrNull(seriesIndex) ?: return emptyList()
    val columnNames = response.columnNames
    val rows = response.rows

    val xAxisIndex = columnNames.indexOf(xAxisColumnName)
    val yAxisIndex = columnNames.indexOf(yAxisColumnName)
    val subgroupIndex = columnNames.indexOf(subgroupColumnName)

    return rows.map { rowElement ->
        val row = (rowElement as? JsonArray) ?: emptyList()
        StatementReportRow(
            yAxis = if (yAxisIndex in row.indices) (row[yAxisIndex] as? JsonPrimitive)?.doubleOrNull ?: 0.0 else 0.0,
            xAxis = if (xAxisIndex in row.indices) (row[xAxisIndex] as? JsonPrimitive)?.content ?: "" else "",
            subgroup = if (subgroupIndex in row.indices) (row[subgroupIndex] as? JsonPrimitive)?.content ?: "" else ""
        )
    }
}