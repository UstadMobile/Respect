# xAPI over IPC

[xAPI](https://www.xapi.com/) over [Inter-process Communication (IPC)](https://source.android.com/docs/core/architecture/ipc/binder-overview).
allows one app on Android to send and receive xAPI data from another app (essentially using the other
app as an xAPI server).

A localhost embedded server on its own won't work because:
* Android will kill background processes e.g. when the client app, such as an instructional app,
  is being used by the user, the server app will be in the background. Within 15 minutes the server
  app process will be killed by Android.
* Power saving restrictions often interfere with networking operations (even localhost) by any app 
  that is in the background.
* Android 16+ introduces [new restrictions on local networking](https://developer.android.com/privacy-and-security/local-network-permission).

__How xAPI over IPC works__:



Sending a request:
Message.what will be XapiIpcWhatFlags.WHAT_REQUEST and Message.arg1 will be the requestId (unique 
for that messenger - eg. set using an Atomic Integer). Message.arg2 will be a constant for the 
endpoint being used.

Sending the response:
Message.what will be XapiIpcWhatFlags.WHAT_RESPONSE and Message.arg1 will be the requestId.

