package world.respect.shared.domain.bookmark
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
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
    suspend operator fun invoke(
        agent: XapiAgent,
        statements: List<XapiStatement>,
    ) {
        val voidingStatements = statements.map { stmt ->
            val stmtId = stmt.id
                ?: throw IllegalStateException("Cannot void bookmark: statement has no id")
            XapiStatement(
                actor = agent,
                verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                `object` = XapiStatementRef(id = stmtId.toString()),
                context = XapiContext(
                    contextActivities = XapiContextActivities(
                        category = listOf(
                            XapiActivity(id = OpenEelXapiConstants.CATEGORY_BOOKMARK_RECIPE)
                        )
                    )
                ),
            )
        }
        if (voidingStatements.isNotEmpty()) {
            schoolDataSource.xapiResource.statements.post(voidingStatements)
        }
    }

    suspend operator fun invoke(
        agent: XapiAgent,
        activityId: String,
    ) {
        val existingStatements = schoolDataSource.xapiResource.statements.get(
            listParams = XapiStatementsResource.GetStatementParams(
                agent = agent,
                verb = XapiVerb.ID_BOOKMARKED,
                activity = activityId,
                relatedActivities = true,
            )
        ).dataOrNull()?.statements ?: emptyList()

        invoke(agent, existingStatements)
    }
}
