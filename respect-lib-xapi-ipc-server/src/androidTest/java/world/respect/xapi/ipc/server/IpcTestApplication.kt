package world.respect.xapi.ipc.server

import android.app.Application
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.XapiResource

class IpcTestApplication: Application(), XapiResourceProvider{

    override fun invoke(
        endpoint: String,
        authentication: String?
    ): XapiResource {
        TODO("Coming soon")
    }

}