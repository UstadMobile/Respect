# HTTP-IPC-Client

Client for [http-ipc](../lib-http-ipc-shared/): used to send http
requests to another app.

```kotlin
val otherAppPackageName = "org.example.other.app"

val client = HttpIpcClientBuilder(context)
    .setIpcServicePackageName(otherAppPackageName)
    .setAuth(auth) //optional: use if required by other app
    .build()

//Now use it the same as OkHttp Client.
val response = client.newCall(
    Request.Builder()
        .url("http://example.org/")
        .build()
).execute()
```
