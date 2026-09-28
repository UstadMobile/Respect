# lib-xapi-ipc-shared guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module provides shared utility functions that are used by the 
[lib-xapi-ipc-client](../lib-xapi-ipc-client) and the [lib-xapi-ipc-server](../lib-xapi-ipc-server)
modules. The lib-xapi-ipc modules enables Experience API (xAPI) communication between two different
Android apps using Android Inter-Process Communication (IPC) where one acts as a server and the
other acts as a client. The Android IPC uses a [bound service Messenger](https://developer.android.com/develop/background-work/services/bound-services#Messenger).
This is required to support offline usage.

## Module guidance

* NEVER use plain unit tests for any test that involves use of parcel file descriptor 
 (e.g. sending an xAPI document over the IPC). ALWAYS use an Android instrumented test.
* NEVER run the adb install command or any other adb command lines directly to run instrumented
  tests. ALWAYS run Android instrumented tests using the IDE tools or Gradle.
