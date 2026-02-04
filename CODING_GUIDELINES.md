
# Coding pattern

The RESPECT app follows an MVVM pattern and [Android Architecture Recommendations](https://developer.android.com/topic/architecture/).

* View layer is built using:
    * [UiState](#uistate-classes) classes contain all information needed to show a screen e.g.
      ```data class PersonDetailUiState(..)```. This often includes entity objects from the data layer.
    * [ViewModels](#viewmodels) (e.g. ```class PersonDetailViewModel```) contain the logic and event handlers for the screen
      (e.g. onClickButton etc) and emit a flow of the UIState class.
    * [Screens](#screens) (e.g. ```fun PersonDetailScreen(viewModel)``` observes the UIState flow from the ViewModel
      to render the user interface (using a Jetpack Compose composable function).
* Domain Layer that contains UseCase(s) as per [Android Architecture Recommendations](https://developer.android.com/topic/architecture/domain-layer)
    * [UseCase](#usecase): where a single class can work for all platforms then a single class can be added
      e.g. ```class AddNewPersonUseCase```. Where different implementations are needed for different
      platforms then create an interface e..g. ```interface OpenExternalLinkUseCase```.
* Data layer: this is an offline-first datalayer that includes a local datasource, remote datasource,
  and a repository to media between the two. See the [respect-datalayer](respect-datalayer/README.md)
  module.

All Kotlin code should follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). SQL queries should follow
[SQLStyle.guide](https://www.sqlstyle.guide/).

## Conventions

All Kotlin code should follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)

All SQL code shoud follow [SQLStyle.guide](https://www.sqlstyle.guide/)

## General coding style

### Very important:

* [Don't repeat yourself (DRY)](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself): follow the 
  don't repeat yourself principle: logic must **NEVER** be repeated or duplicated by copy/paste, 
  typing the same thing again, or making a completely new code that does almost the same thing as 
  existing code. Use domain use cases, inheritence, and extension functions as appropriate. When
  logic is duplicated, any bug would also be duplicated, maintenance will be harder, the code will 
  be harder to understand, etc.
* Don't silently ignore null instead of throwing exceptions/showing error messages: do not use 
  inappropriate ```?.let```, ```?.also``` etc. Code that silently ignores when something is wrong 
  is **MUCH** worse (and harder to debug/find any error) than code that throws an exception. Don't 
  make variables nullable when they should not be. Don't silently ignore null. Don't fail to throw 
  exceptions when something can go wrong. Those exceptions should be caught/displayed/logged appropriately.
* Don't ignore pull request / task guidance/feedback. Feedback/guidance can be freely questioned 
  and alternatives suggested, but not ignored.
* Never comment out code instead of deleting it, unless there is a defined reason why it needs 
  temporarily disabled and when it will be restored.
* Never use hardcoded string literals for text that will appear in the user interface. Use 
  [strings.xml](respect-lib-shared/src/commonMain/composeResources/values/strings.xml) so they are 
  localizable.
* Never manually specify UI styles unless explicitly noted. Use the theme defaults so that the UI 
  will be consistent and handle dark mode, different default font size preferences, etc appropriately.
* Always provide a contentDescription for any icon button that does not have text for accessibility.

#### Avoid terms that could reasonably be considered racist and/or discriminatory

e.g. use:
```
primary, replica, allowlist, blocklist
```

Do not use:
```
master, slave, whitelist, blacklist
```

#### **Do not ever use !! in production Kotlin code.** The !! operator is OK in unit tests,
but should never be used in non-test code.

e.g. use:

```
someEntityName?.also { entityVal ->
   //Smart cast
}

```
Do not do this:
```
if(someEntity?.name != null) {
    println(someEntity!!.name!!)
}
```

use:
```
memberVar = SomeEntity().apply {
   someField = "aValue"
}
```

Do not do this:
```
memberVar = SomeEntity()
memberVar!!.someField = "aValue"
```

Do not use null checks that fail silently and would lead to code that doesn't behave as expect with 
no logging output or exception

e.g.
```
somethingThatShouldntBeNullNow?.also {
   it.doWork()
}
```
This code will silently fail to do anything. A situation like this should normally throw an exception, 
and at a minimum MUST be logged.

#### Never hardcode any literal values

The following can be hardcoded:
* 0, 1, -1, true, false, null, "" (empty string)
* Strings less than 64 characters where the string is defined as part of an external specification 
  that we are implementing.

Any other literal must not be hardcoded and there should be only one constant value defined for a 
given purpose (such that if this value needs changed in future, it should only need changed in one place).

e.g.
Do not do this:
```
class MyClass {
    val byteArray = ByteArray(8192)
}
```

Do this:
```
class MyClass {
    val byteArray = ByteArray(DEFAULT_BUFFER_SIZE) 

    companion object {
        const val DEFAULT_BUFFER_SIZE = 8192
    }
}

```

#### Cite references in comments where needed to understand or verify the code

If a section of code is following a particular official reference (e.g. Android, Kotlin, API specification), that is important to verifying that the code is correct, sensible, etc. then cite the reference in comments.

#### Don't hide exceptions

If a function's signature is doSomething, when something cannot be done as expected, it _should_ throw an exception. The exception should be
caught and explicitly handled where appropriate (e.g. in the viewmodel to show that an operation failed, retry logic, etc). Just printing/logging
an exception hides it, leading to subsequent code running when it probably shoudln't. Logging _and_ rethrowing can be a good idea.

## Use of AI tools

AI generated code is prone to errors, and the code generated often looks like it _should_ be right, but
isn't. AI tools should be used _only when the author is confident that they can inspect the generated
code and spot such mistakes/errors_. All code must still adhere to this coding style. Sometimes it 
will be quicker to spend one hour writing something manually, rather than spending many more hours 
trying to debug the code the AI wrote in 30 seconds.

## View layer

### UiState classes

The UiState class contains everything needed to render a screen. It is emitted as a flow from the
ViewModel. It is a data class contained in the same file as the ViewModel.

The UiState class should also contain the model entity. It should use the same model type as found
on the view (e.g. because PersonDetailView uses PersonWithPersonParentJoin, PersonDetailUiState
should contain PersonWithPersonParentJoin).

```
data class PersonDetailUiState(
    val person: PersonWithPersonParentJoin? = null,

    val changePasswordVisible: Boolean = false,

    val showCreateAccountVisible: Boolean = false,

    val chatVisible: Boolean = false,

    val clazzes: List<ClazzEnrolmentWithClazzAndAttendance> = emptyList(),
) {
    
    //Where view information is derived from other parts of the state, use a simple getter e.g.
    val emailAddressVisible: Boolean
        get() = !person?.emailAddress.isNullOrEmpty()
}
```

### ViewModels

The ViewModel is responsible for all business logic. It emits a flow of the UiState class, which is
observed and rendered by the view. It has event handling functions that can be called by the view 
when events take place (e.g. when a user clicks a button).

The BaseName should be suffixed with List, Detail, or Edit to describe it's function where 
appropriate:

*Name*ListViewModel, *Name*DetailViewModel, *Name*EditViewModel for list, detail, and edit screens
e.g. *ContentEntry*ListViewModel, *ContentEntry*DetailViewModel, *ContentEntry*EditViewModel.

e.g.

```
class PersonDetailViewModel: ViewModel {
    val uiState: Flow<PersonDetailUiState>
    
    init {
        //Logic to seutp the uiState here
    }
    
    //Event handlers here
    fun onClickCreateAccount() {
    
    }
    
    fun onClickChat() {
    
    }
    
    fun onClickClazz(clazz: ClazzEnrolmentWithClazzAndAttendance) {

    }
}

```

### Screens

Screens are written using Jetpack Compose. The screen function should use the UiState as an argument.

Screens must use default margins and colors provided by the platform theme unless specifically noted otherwise. On Jetpack compose
this is 16.dp between screen edge and components, and between components. ```ListItem``` already includes padding so it should not be added. Components other
than ListItem should use ```Modifier.defaultItemPadding()``` for this.

Dates, times, and timestamps (e.g. date and time combined) are formatted using ```rememberDateFormat``` and ```useFormattedDate``` functions etc. These
functions use the user's locale to format date/time accordingly. Unless otherwise noted in writing, any date, time, or timestamp should
use the existing formatters (the formatting shown on the prototype may vary from the result of using the function, and the functions result will be
deemed correct.

Android Jetpack Compose:
```
/*
 * Main composable function: this should always take the UI state as the first parameter, and then
 * have parameters for event handlers.
 */ 
@Composable
function PersonDetailScreen(
    uiState: PersonDetailUiState = PersonDetailUiState(),
    onClickCreateAccount: () -> Unit,
    onClickChat: () -> Unit,
    onClickClazz: (ClazzEnrolmentWithClazzAndAttendance) -> Unit,
) {
    //UI functions go here eg.
    Row {
        if(uiState.chatVisible) {
            Button(onClick = onClickChat) {
                Text(stringResource(R.id.chat))
            }
        }
        
        //Use the object on the UiState to show properties
        Text(uiState.person?.firstNames + uiState.person?.lastName)
       
        Text(uiState.person?.phoneNumber)
    }
}

/*
 * Note: different function name is used to avoid rendering issues in Android studio if using
 * the same function name with different parameters (which is valid and will compile, but then the 
 * the preview in Android Studio sometimes won't work).
 */
@Composable
function PersonDetailScreenForViewModel(
    viewModel: PersonDetailViewModel
) {
    val uiState: PersonDetailUiState by viewModel.uiState.collectAsState(initial = null)
    
    //Always use named arguments here e.g. onClickChat to avoid potential mismatch.
    PersonDetailScreen(
        uiState = uiState, 
        onClickChat = viewModel::onClickChat,
        onClickClazz = viewModel::onClickClazz, 
        onClickCreateAccount = viewModel::onClickCreateAccount
    )
}

@Composable
@Preview
function PersonDetailScreenPreview(
    uiState = PersonDetailUiState(
         person = Person().apply {
              firstNames = "Preview"
              lastName = "Person"
         }
    )
)
```


## Domain Layer

### UseCase

A UseCase will be named as per the Android architecture recommendations in the form of Verb(Noun-optional)UseCase
and will contain a single invoke function. A UseCase can depend on other UseCases which should be provided as
constructor parameters. Constructor parameters should be dependencies (other use cases, serialization tools, etc). Anything that
changes per invocation (e.g. input parameters, output parameters, progress listeners, etc) should be arguments for the invoke function.

e.g.
```
class DoThingsUseCase(
   private val dependency: OtherUseCase,
) {
    data class WhatToDo(
        val what: String,
        val howMuch: Int,
    )

    data class DoThingsResult(
         val howMuchDone: Int,
         val notDone: List<String>
    )

    suspend operator fun invoke(
        todo: List<WhatToDo>,
        progressListener: (Int) -> Unit,
    ) {
        //.. do stuff here
        return ToDoThingsResult(..)
    }
}
```

Where different implementations are required for different platforms the UseCase itself should be an
interface (e.g. ```interface DoThingsUseCase```) and then implementations for each platform should be
in the same package (e.g. ```class DoThingsUseCaseJvm```, ```class DoThingsUseCaseAndroid```,
```class DoThingsUseCaseJs``` etc)

The UseCase should be bound using the dependency injection (Koin).

### Spelling

Use US English spellings, the same as system libraries etc.

### Localization strings

The name for the string should be just the string itself. Only add extra text if the translation would be different due to a different context.

use:
```
<string name="download">Download</string>
```

Do not use this:
```
<string name="myscreen_download">Download</string>
```

Where some context might be needed to translate this make sure to put a comment before:
e.g.
```
<!-- Used to set the title on an edit screen where the user is creating a new entity e.g.
new class, new assignment, etc. %1$s will be replaced with the name of the item.-->
<string name="new_entity">New %1$s</string>
```



