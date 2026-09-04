package org.openeel.lib.ipc.messagebridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class ServiceConnectionMessengerProvider(
    private val context: Context,
    private val intent: Intent,
) : MessengerProvider, Closeable {

    private var mMessenger: Messenger? = null

    private val closed = AtomicBoolean(false)

    private val messengerFuture = AtomicReference<CompletableFuture<Messenger>>(
        CompletableFuture()
    )

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName,
            service: IBinder,
        ) {
            Log.i(LOGTAG, "ServiceConnectionMessengerProvider: onServiceConnected")
            mMessenger = Messenger(service).also {
                messengerFuture.get().complete(it)
                Log.i(LOGTAG, "ServiceConnectionMessengerProvider: onServiceConnected: future completed")
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(LOGTAG, "ServiceConnectionMessengerProvider: onServiceDisconnected")
            mMessenger = null
            messengerFuture.set(CompletableFuture())
        }
    }


    init {
        Log.i(LOGTAG, "ServiceConnectionMessengerProvider: init: action=${intent.action}")
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun invoke(): Messenger {
        if(closed.get()) {
            throw IllegalStateException("ServiceConnectionMessengerProvider: already closed")
        }

        return messengerFuture.get().join()
    }

    override fun close() {
        if(!closed.getAndSet(true)) {
            context.takeIf { messengerFuture.get().isDone }?.unbindService(mConnection)
        }
    }

    companion object {

        const val LOGTAG = "HttpIpc"

    }
}