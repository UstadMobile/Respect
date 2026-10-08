# lib-xapi-ipc-client guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module provides an implementation of [XapiResource](../lib-xapi-core/src/commonMain/kotlin/world/respect/lib/xapi/resources/XapiResource.kt)
that works by connecting to the XapiIpcService in the [lib-xapi-ipc-server](../lib-xapi-ipc-server) 
module. This is normally used as a dependency in other apps to allow them to connect to the launcher
app over IPC to send/receive Experience API data.

You should read [lib-xapi-ipc-shared AGENTS.md](../lib-xapi-ipc-shared/AGENTS.md) before working with
code in this module.

## Module guidance

* NEVER use plain unit tests for any test that involves use of parcel file descriptor
  (e.g. sending an xAPI document over the IPC). ALWAYS use an Android instrumented test.
* NEVER run the adb install command or any other adb command lines directly to run instrumented
  tests. ALWAYS run Android instrumented tests using the IDE tools or Gradle.
