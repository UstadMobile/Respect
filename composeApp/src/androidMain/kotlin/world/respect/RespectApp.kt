package world.respect


import android.app.Application
import org.kodein.di.DI
import org.kodein.di.DIAware

class RespectApp : Application(), DIAware {
    override val di = DI.lazy {
        // Initialize your DI container and bind the dependencies
    }
}
