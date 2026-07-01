package world.respect.shared.domain.bookmark

import world.respect.datalayer.SchoolDataSource
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb

/**
 * UseCase to add a bookmark for a learning unit by posting a bookmark xAPI statement.
 */
class AddBookmarkUseCase(
    private val schoolDataSource: SchoolDataSource,
) {

    suspend operator fun invoke(
        agent: XapiAgent,
        activityId: String,
    ) {
        val bookmarkStatement = XapiStatement(
            actor = agent,
            verb = XapiVerb(id = XapiVerb.ID_BOOKMARKED),
            `object` = XapiActivity(id = activityId),
        )
        schoolDataSource.xapiResource.statements.post(listOf(bookmarkStatement))
    }
}

