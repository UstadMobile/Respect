# lib-xapi-ipc-server guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module provides an Android bound service based on [android.os.Messenger] that is used to 
provide an Experience API (xAPI) Server that other native Android apps can connect to using the 
[lib-xapi-ipc-client module](../lib-xapi-ipc-client) as a dependency.

You should read [lib-xapi-ipc-shared AGENTS.md](../lib-xapi-ipc-shared/AGENTS.md) before working with
code in this module.

## Module guidance

* NEVER use plain unit tests for any test that involves use of parcel file descriptor 
 (e.g. sending an xAPI document over the IPC). ALWAYS use an Android instrumented test.
* NEVER run the adb install command or any other adb command lines directly to run instrumented
  tests. ALWAYS run Android instrumented tests using the IDE tools or Gradle.
