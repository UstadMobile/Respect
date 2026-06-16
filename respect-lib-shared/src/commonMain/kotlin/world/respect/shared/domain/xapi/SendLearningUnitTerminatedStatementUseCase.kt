package world.respect.shared.domain.xapi

import kotlinx.coroutines.flow.firstOrNull
import org.koin.mp.KoinPlatform.getKoin
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.lib.xapi.model.XapiVerb
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

class SendLearningUnitTerminatedStatementUseCase(
    private val accountManager: RespectAccountManager,
) {

    suspend operator fun invoke(
        activityId: String,
    ) {
        val account = accountManager.activeAccount ?: return

        val accountScope = accountManager.getOrCreateAccountScope(account)

        val schoolDataSource: SchoolDataSource = accountScope.get()

        val actor = accountManager.selectedAccountAndPersonFlow.firstOrNull()?.xapiAgent
            ?: return

        schoolDataSource.xapiResource.statements.post(
            listOf(
                createLearningUnitStatement(
                    activityId = activityId,
                    actor = actor,
                    verbId = XapiVerb.ID_TERMINATED,
                )
            )
        )
    }
}