package world.respect.server

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.scope.Scope
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.server.domain.school.migrate.migrateSchoolAppsToXapi
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.util.di.RespectAccountScopeId
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * Manage creating per-account scopes on the server side and link them in a concurrent-safe way to
 * the related school scope.
 *
 * Per-account Koin dependencies on the server are created using a factory (so they are not
 * needlessly retained). Koin can invoke a factory concurrently. Any concurrent attempt to link
 * scopes will cause a ConcurrentModificationException to be thrown.
 *
 * The ServerAccountManager is scoped (and retained as a singleton) at in the school scope.
 */
class ServerAccountScopeManager(
    private val schoolUrl: Url,
    private val schoolScope: Scope,
): KoinComponent {

    private val lock = ReentrantLock()

    @Volatile
    private var appsMigrated = false
    fun getOrCreateAccountScope(
        authenticatedUserPrincipalId: AuthenticatedUserPrincipalId
    ): Scope {
        val accountScopeId = RespectAccountScopeId(schoolUrl, authenticatedUserPrincipalId)
        return lock.withLock {
            val accountScope = getKoin().getScopeOrNull(accountScopeId.scopeId)
                ?: getKoin().createScope(
                    accountScopeId.scopeId, TypeQualifier(RespectAccount::class)
                ).also { it.linkTo(schoolScope) }

            if (!appsMigrated) {
                try {
                    runBlocking { migrateSchoolAppsToXapi(schoolScope) }
                    appsMigrated = true
                } catch (e: Exception) {
                    Napier.e("App->xAPI migration failed for $schoolUrl", e)
                }
            }

            accountScope
        }
    }


}