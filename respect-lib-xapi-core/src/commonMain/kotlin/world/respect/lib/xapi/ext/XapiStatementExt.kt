package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiStatement

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


