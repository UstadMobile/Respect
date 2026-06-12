# Xapi over IPC

Sends and receives xAPI over an IPC messenger.

How it works:

Sending a request:
Message.what will be XapiIpcWhatFlags.WHAT_REQUEST and Message.arg1 will be the requestId (unique 
for that messenger - eg. set using an Atomic Integer). Message.arg2 will be a constant for the 
endpoint being used.

Sending the response:
Message.what will be XapiIpcWhatFlags.WHAT_RESPONSE and Message.arg1 will be the requestId.

