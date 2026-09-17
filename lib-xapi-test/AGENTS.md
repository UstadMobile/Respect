# lib-xapi-test guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module contains Experience API (xAPI) assertions that are used to verify the behavior of
different implementations of an xAPI resource (e.g. a database implementation, http server/client
implementation, offline first repository implementation, etc). Because any xAPI implementation 
should behave the same way, regardless of the underlying implementation, the same tests can be used
for all implementations.

The assertions are based on the xAPI specifications.

Before working on this module you should read the [lib-xapi-core module AGENTS.md](../lib-xapi-core/AGENTS.md).

## Assertion examples

Example 1: Assertion comparing expected/actual model objects (test comparison between objects based 
on xAPI specification):
```kotlin
fun assertXapiStatementCanonicallyEqual(
    expected: XapiStatement,
    actual : XapiStatement,
    idOnlyFormat: Boolean = false,
    messagePrefix: String = "",
)
```

Example 2: Assertion on the behavior of a resource (eg that it can store and retrieve data as 
expected):

```kotlin
suspend fun assertStatementCanBeStoredAndRetrieved(
    statement: XapiStatement,
    resource: XapiStatementsResource
)
```

Assertions of behavior will rely on the aforementioned comparison assertions (e.g. to test that
data stored and retrieved is canonically equal as per the specification).
