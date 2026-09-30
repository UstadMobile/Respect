package world.respect

import android.app.Activity
import android.content.Intent
import io.ktor.http.Url
import kotlinx.coroutines.CompletableDeferred
import world.respect.shared.domain.account.authwithopenid.OpenIdAuthorizationResult
import world.respect.shared.domain.account.authwithopenid.OpenIdAuthorizationUseCase
import world.respect.shared.domain.activitycontextjobprocessor.EnqueueActivityContextJobUseCase
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class OpenIdAuthorizationUseCaseAndroid(
    private val enqueueActivityContextJobUseCase: EnqueueActivityContextJobUseCase,
) : OpenIdAuthorizationUseCase {

    private val pendingResults = ConcurrentHashMap<String, CompletableDeferred<OpenIdAuthorizationResult>>()

    override suspend operator fun invoke(issuer: Url): OpenIdAuthorizationResult {
        val requestId = UUID.randomUUID().toString()
        val result = CompletableDeferred<OpenIdAuthorizationResult>()
        pendingResults[requestId] = result

        try {
            enqueueActivityContextJobUseCase { activity: Activity ->
                activity.startActivity(
                    Intent(activity, OpenIdAuthorizationActivity::class.java).apply {
                        putExtra(OpenIdAuthorizationActivity.EXTRA_REQUEST_ID, requestId)
                        putExtra(OpenIdAuthorizationActivity.EXTRA_ISSUER, issuer.toString())
                    }
                )
            }

            return result.await()
        } finally {
            pendingResults.remove(requestId)
        }
    }

    override fun publishResult(result: OpenIdAuthorizationResult) {
        pendingResults.remove(result.requestId)?.complete(result)
    }
}
