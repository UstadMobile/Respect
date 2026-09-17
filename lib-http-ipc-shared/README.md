# HTTP-IPC

HTTP-IPC allows Android apps to send/receive http requests directly between apps. Unlike normal
http to/from a server it works offline. Unlike an embedded server it is not affected by 
[restrictions on local networking](https://developer.android.com/privacy-and-security/local-network-permission) 
and the bound service ensures that the server app is not killed when in the background servicing the
client app.

This is implemented using a [Bound Messenger Service](https://developer.android.com/develop/background-work/services/bound-services#Messenger). 
Headers, status codes, etc are sent as Bundle fields. Bodies streamed using a ParcelFileDescriptor
pipe.

Note: other background restrictions, including on network access, still apply. If the server
app (in the background) itself tries to make a request to the network (instead of answering it using
locally available data), it is quite likely that the network request will fail.

