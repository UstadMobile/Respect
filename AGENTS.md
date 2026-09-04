# Respect project guide

This file provides guidance for AI agents working with code in this repository.

## Repository overview

This is the Kotlin programming language repository containing:
* The Respect Android app: [app-android module](app-android/)
* Compose Multiplatform UI: [respect-app-compose module](respect-app-compose/)
* ViewModels and domain logic architecture: [respect-lib-shared module](respect-lib-shared/)
* The datalayer is an offline-first datalayer in the modules [respect-datalayer (interfaces/abstract classes)](respect-datalayer/),
  [respect-datalayer-db (local database layer)](respect-datalayer-db),
  [respect-datalayer-http (http datasource implementation)](respect-datalayer-http),
  [respect-datalayer-repository (offline-first repository implementation)](respect-datalayer-repository),
* Server built using KTOR: [respect-server module](respect-server/)
* The main Android Activity is `app-android/src/main/kotlin/world/respect/MainActivity.kt`
* The NavHost with composable routes in ```respect-app-compose/src/commonMain/kotlin/world/respect/app/app/AppNavHost.kt```

BEFORE running tests, modifying, or investigating code - identify the module and READ the
AGENTS.md file for that module.

## Repository wide patterns to follow
* Use Koin for dependency injection. Dependencies can be:
  * Singletons: one instance for the whole application
  * School scope: one instance that is tied to a particular school/http API enpdoint (e.g. the SQLite 
    Room database for a given school, an AcceptInviteUseCase, etc).
  * Account scope: one instance tied to a particular account. This is a child scope of the school scope. It can 
    access dependencies from the related school scope (but not vice versa). Used for datasources
    (where the account determines permissions rules), authentication use cases, etc.

## Kotlin Programming Language Anti-Patterns: - Never Generate these

## 1. Coroutines:
NEVER: GlobalScope.launch { } 

ALWAYS: viewModelScope.launch { } or lifecycleScope.launch { } 

## 2. Null Safety:
NEVER: value!! (non-null assertion)

ALWAYS: value?.let { } or value ?: defaultValue

## 3. Data classes
NEVER: data class Foo(var x: Int)

ALWAYS: data class Foo(val x: Int)

## 4. Sealed Classes

ALWAYS: use when{} as expression, NEVER as statement.

