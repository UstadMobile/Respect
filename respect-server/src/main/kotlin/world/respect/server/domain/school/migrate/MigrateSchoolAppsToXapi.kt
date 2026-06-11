package world.respect.server.domain.school.migrate

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import org.koin.core.scope.Scope
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.school.xapi.XapiStatementsResourceLocal
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.server.ServerAccountScopeManager
import world.respect.shared.domain.xapi.createBlankAppListingStatement
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

/**
 * Migration utility to move apps from the legacy SchoolApp table to the modern xAPI stream.
 */
suspend fun migrateSchoolAppsToXapi(schoolScope: Scope) {
    val schoolDb: RespectSchoolDatabase = schoolScope.get()
    val schoolUrl: Url = SchoolDirectoryEntryScopeId.parse(schoolScope.id).schoolUrl

    // Access the Admin scope to perform the write
    val accountScope = schoolScope.get<ServerAccountScopeManager>()
        .getOrCreateAccountScope(AuthenticatedUserPrincipalId("1"))

    val xapiResource: XapiStatementsResourceLocal =
        accountScope.get<SchoolDataSourceLocal>().xapiStatementsResource

    val legacyApps = schoolDb.getSchoolAppEntityDao().list(includeDeleted = false)
    if (legacyApps.isEmpty()) return

    // Idempotency: find what's already migrated
    val alreadyListed = xapiResource.get(
        listParams = XapiStatementsResource.GetStatementParams(
            verb = XapiVerb.ID_LISTED_APP,
            activity = OpenEelXapiConstants.CATEGORY_APP_LISTING_RECIPE,
            relatedActivities = true,
        ),
        dataLoadParams = DataLoadParams(),
    ).dataOrNull()?.statements
        ?.mapNotNull { it.objectActivityOrNull()?.id }
        ?.toSet() ?: emptySet()

    // Create the Admin Agent
    val adminActor = XapiAgent(
        account = XapiAccount(homePage = schoolUrl.toString(), name = "1")
    )

    // Map legacy apps to xAPI
    val statements = legacyApps.mapNotNull { app ->
        val manifest = app.saManifestUrl.toString()
        if (manifest in alreadyListed) return@mapNotNull null

        createBlankAppListingStatement(
            appActivityId = manifest,
            appTitle = emptyMap(),
            actor = adminActor,
            manifestUrl = manifest,
        )
    }

    if (statements.isEmpty()) return

    // Perform the migration
    xapiResource.updateLocal(statements, forceOverwrite = false)
    Napier.i("Migrated ${statements.size} apps to xAPI for $schoolUrl")
}