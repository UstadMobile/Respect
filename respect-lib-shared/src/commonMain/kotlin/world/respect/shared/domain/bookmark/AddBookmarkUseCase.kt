package world.respect.shared.domain.bookmark

import world.respect.datalayer.SchoolDataSource
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.toStringMap
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb

class AddBookmarkUseCase(
    private val schoolDataSource: SchoolDataSource,
) {

    suspend operator fun invoke(
        agent: XapiAgent,
        activityId: String,
        title: LangMap? = null,
    ) {
        val bookmarkStatement = XapiStatement(
            actor = agent,
            verb = XapiVerb(id = XapiVerb.ID_BOOKMARKED),
            `object` = XapiActivity(
                id = activityId,
                definition = title?.let {
                    XapiActivityDefinition(
                        name = it.toStringMap(noLangKey = XAPI_NO_LANG_KEY)
                    )
                }
            ),
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

    companion object {

        /**
         * a LangMapStringValue has no language key.
         */
        const val XAPI_NO_LANG_KEY = "und"
    }
}

