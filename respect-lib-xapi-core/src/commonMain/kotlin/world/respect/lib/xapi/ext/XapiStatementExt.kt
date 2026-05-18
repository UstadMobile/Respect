package world.respect.lib.xapi.ext

import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiStatement
import kotlin.time.Instant

/**
 * Add the assignmentActivityId to the statement as per the Assignment Recipe, if not already present.
 *
 * @param assignmentActivityId the activity id of the assignment
 * @return the updated statement
 */
fun XapiStatement.asAssignmentRecipeStmt(
    assignmentActivityId: String
): XapiStatement {

    val idInGrouping = context?.contextActivities?.grouping?.any {
        it.id == assignmentActivityId
    } == true

    return if(!idInGrouping) {
        this.copy(
            context = (context ?: XapiContext()).copy(
                contextActivities = (context?.contextActivities ?: XapiContextActivities()).copy(
                    grouping = buildList {
                        context?.contextActivities?.grouping?.also {
                            addAll(it)
                        }
                        add(XapiActivity(id = assignmentActivityId))
                    }
                )
            )
        )
    }else {
        this
    }
}

fun XapiStatement.asAssignmentRecipeStmtIfIdNotNull(
    assignmentActivityId: String?
): XapiStatement {
    return assignmentActivityId?.let {
        this.asAssignmentRecipeStmt(it)
    } ?: this
}

/**
 * Get the last-modified for a list of statements being returned as per
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
 */
fun List<XapiStatement>.lastModifiedForRetrievedStatements(
    ascendingOrder: Boolean = true,
    consistentThrough: Instant,
): Instant {
    return if(ascendingOrder && isNotEmpty()) {
        last().stored ?: throw IllegalArgumentException("Retrieved statement should have stored set")
    }else if(!ascendingOrder && isNotEmpty()) {
        first().stored ?: throw IllegalArgumentException("Retrieved statement should have stored set")
    }else {
        consistentThrough
    }
}

fun List<XapiStatement>.lastModifiedGMTStringForRetrievedStatements(
    ascendingOrder: Boolean,
    consistentThrough: Instant,
) : String {
    return GMTDate(
        lastModifiedForRetrievedStatements(
            ascendingOrder, consistentThrough
        ).toEpochMilliseconds()
    ).toHttpDate()
}


