package world.respect.shared.domain.xapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import world.respect.datalayer.school.model.report.ReportOptions
import world.respect.datalayer.school.model.report.StatementReportRow
import world.respect.lib.opds.model.toStringMap
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.OpenEelXapiConstants.CATEGORY_REPORT_QUERY_RECIPE
import world.respect.lib.xapi.ext.encodeWithExtension
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiActivityDefinition.Companion.TYPE_REPORT
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.model.XapiVerb.Companion.ID_REPORT_QUERY_REQUEST
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


const val COLUMN_NAMES = "columnNames"
const val ROWS = "rows"
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
    query: String? = null,
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
                name = reportOptions.title.toStringMap(),
                description = reportDescription,
            ).encodeWithExtension(
                json = json,
                extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_QUERY,
                serializer = kotlinx.serialization.serializer(),
                value = query ?: ""
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


/**
 * Extension function that retrieves the report query result extension object.
 */
fun XapiStatement.getQueryResultExtension(): JsonObject? {
    return result?.extensions?.get(OpenEelXapiConstants.EXTENSION_REPORT_QUERY_RESULT) as? JsonObject
}

fun XapiStatement.getQueryResultColumnNames(): List<String> {
    return getQueryResultExtension()
        ?.get(COLUMN_NAMES)
        ?.let { it as? JsonArray }
        ?.mapNotNull { (it as? JsonPrimitive)?.content }
        ?: emptyList()
}

fun XapiStatement.getQueryResultRows(): List<List<JsonPrimitive>> {
    return getQueryResultExtension()
        ?.get(ROWS)
        ?.let { it as? JsonArray }
        ?.map { rowElement ->
            (rowElement as? JsonArray)
                ?.mapNotNull { it as? JsonPrimitive }
                ?: emptyList()
        }
        ?: emptyList()
}

fun XapiStatement.toStatementReportRows(
    xAxisColumnName: String = COLUMN_NAME_X_AXIS,
    yAxisColumnName: String = COLUMN_NAME_Y_AXIS,
    subgroupColumnName: String = COLUMN_NAME_SUBGROUP
): List<StatementReportRow> {
    val columnNames = getQueryResultColumnNames()
    val rows = getQueryResultRows()

    val xAxisIndex = columnNames.indexOf(xAxisColumnName)
    val yAxisIndex = columnNames.indexOf(yAxisColumnName)
    val subgroupIndex = columnNames.indexOf(subgroupColumnName)

    return rows.map { row ->
        StatementReportRow(
            yAxis = if (yAxisIndex in row.indices) row[yAxisIndex].doubleOrNull ?: 0.0 else 0.0,
            xAxis = if (xAxisIndex in row.indices) row[xAxisIndex].content else "",
            subgroup = if (subgroupIndex in row.indices) row[subgroupIndex].content else ""
        )
    }
}