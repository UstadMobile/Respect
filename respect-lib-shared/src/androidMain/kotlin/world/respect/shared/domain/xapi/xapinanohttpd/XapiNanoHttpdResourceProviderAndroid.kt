package world.respect.shared.domain.xapi.xapinanohttpd

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdResourceProvider
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.util.di.RespectAccountScopeId

class XapiNanoHttpdResourceProviderAndroid: XapiNanoHttpdResourceProvider, KoinComponent {

    override fun invoke(
        endpoint: Url,
        authentication: String,
    ): XapiStatementsResource {
        val accountScope = RespectAccountScopeId(
            schoolUrl = endpoint, accountPrincipalId = AuthenticatedUserPrincipalId(authentication),
        )
        val schoolDataSource: SchoolDataSource = getKoin().getScope(accountScope.scopeId).get()
        return schoolDataSource.xapiStatementsResource
    }
}