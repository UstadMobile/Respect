package org.openeel.lib.ipc.messagebridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

class ServiceConnectionMessengerProvider(
    private val context: Context,
    private val intent: Intent,
) : MessengerProvider {

    private var mMessenger: Messenger? = null

    private val messengerFuture = AtomicReference<CompletableFuture<Messenger>>()

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName,
            service: IBinder,
        ) {
            mMessenger = Messenger(service).also {
                messengerFuture.get().complete(it)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            messengerFuture.set(CompletableFuture())
        }
    }


    init {
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun invoke(): Messenger {
        return messengerFuture.get().join()
    }
}