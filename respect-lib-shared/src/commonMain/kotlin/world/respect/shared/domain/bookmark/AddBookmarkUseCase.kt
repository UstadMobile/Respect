package world.respect.shared.domain.bookmark

import io.ktor.http.Url
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb

/**
 * UseCase to add a bookmark for a learning unit by posting a bookmark xAPI statement.
 *
 * The appManifestUrl is stored in context.contextActivities.parent so it can be retrieved when
 * loading bookmarks without needing to resolve app URLs separately.
 */
class AddBookmarkUseCase(
    private val schoolDataSource: SchoolDataSource,
) {

    suspend operator fun invoke(
        agent: XapiAgent,
        activityId: String
    ) {
        val bookmarkStatement = XapiStatement(
            actor = agent,
            verb = XapiVerb(id = XapiVerb.ID_BOOKMARKED),
            `object` = XapiActivity(id = activityId),
            context = XapiContext(
                contextActivities = XapiContextActivities(
                    category = listOf(
                        XapiActivity(id = OpenEelXapiConstants.CATEGORY_BOOKMARK_RECIPE)
                    )
                )
            ),
        )
        schoolDataSource.xapiResource.statements.post(listOf(bookmarkStatement))
    }
}

