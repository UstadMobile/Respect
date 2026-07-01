package world.respect.shared.domain.bookmark
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
/**
 * UseCase to remove a bookmark by voiding the bookmark xAPI statement(s) for a given activity.
 */
class RemoveBookmarkUseCase(
    private val schoolDataSource: SchoolDataSource,
) {
    /**
     * @param agent the agent whose bookmark to remove
     * @param activityId the activity ID of the bookmarked learning unit
     */
    suspend operator fun invoke(
        agent: XapiAgent,
        activityId: String,
    ) {
        val existingStatements = schoolDataSource.xapiResource.statements.get(
            listParams = XapiStatementsResource.GetStatementParams(
                agent = agent,
                verb = XapiVerb.ID_BOOKMARKED,
                activity = activityId,
            )
        ).dataOrNull()?.statements ?: emptyList()
        existingStatements.forEach { stmt ->
            val stmtId = stmt.id
                ?: throw IllegalStateException("Cannot void bookmark: statement has no id")
            schoolDataSource.xapiResource.statements.post(
                listOf(
                    XapiStatement(
                        actor = agent,
                        verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                        `object` = XapiStatementRef(id = stmtId.toString()),
                    )
                )
            )
        }
    }
}
