# Design Guidelines

### General

* Designs follow [Material3](https://m3.material.io/) guidelines (unless there is an absolute need / justification to not do so in a specific case)
* Designs _always_ follow patterns seen in other widely used apps
* Designs _always_ follow patterns in the RESPECT app as currently published unless there is a noted reason to do otherwise.
* Where the on screen keyboard would likely cover textfields (e.g. an edit screen with more than 2 textfields), then the action button (e.g. next/done/save) should be in the top right
* Where a user's actions are saved to the database/server, the the action text should be __Save__. Where the changes are not directly saved (e.g. when the user is taken
  another screen to edit a component (such as the filter in a report) then the text should be __Done__.
* When a user creates/adds a new entity (e.g. class, assignment, person) and clicks __Save__ the user is taken to the detail screen for the entity they
  just created (prototypes might not get the navigation to skip the edit screen if user then clicks back, but must still follow the pattern of taking
  a user to the detail screen after clicking Save on adding a new entity).
* Screens should be as intuitive as possible. Explicit text explanations of what to do next are a _last resort_ (e.g. as used with passkeys, as per
  Google's UX guidance because users are not familiar with them).

### Final designs for development:

* Should be unambiguous to any reasonable developer (covering all reasonably forseeable scenarios). It must be clear what behavior is expected. Everything needed
  to understand the expected behavior must be included within:
    * The prototype
    * The existing app
    * The standard behaviors noted below
    * Behavior notes on the Github task card issue (can be used to explain behavior that is difficult to explain using the prototype itself e.g. the minimum and maximum length of a PIN)
    * _If unavoidable_ another prototype: e.g. where one task depends on another and is expected to be completed in parallel.  This should be avoided as far as possible, because it means the task when completed could
      only be provided to users when the other task is also completed. E.g. the add school self-service system task uses the invitation task. If another prototype is being referenced,
      it must be explicitly noted and linked on the task card.
* Screens must be linked so that the developer can understand the flow.
* Should not include existing screens that are not going to be modified within the scope of the task.
  Exception: where clicking on a new or modified screen (A) takes a user to an existing screen (B) that is not
  going to be modified, screen B itself should be included. Nothing on screen B should be clickable.

### Standard behavior unless noted otherwise:

Behaviors below do not need to be included in prototypes. They must be implemented by developers unless it is explicitly noted otherwise.

* If a required field is left blank and the user clicks Save/Submit/Next etc, the field should show as red (as an error) with
  the supporting text (underneath field) that shows "Required field".
* When a new entity is being added, the app title should say Add new entity (e.g. Add new class). When an existing entity is being
  modified, the app title should show "Edit entity" (e.g. Edit class). The add new and edit screen are the same screen.
* When the user clicks __Save__ after adding a new entity:
   * If in picker mode the user will be returned from where the pick started (e.g. if the user was in a class detail screen,
     then selected to add a student, and then selected add a new person, filled in the details and clicked save for the new
     person, they are returned to the class detail screen).
   * Otherwise: the user is taken to the detail screen for the entity they just created. If the user clicks back, then they go
     back to where they were before the edit screen (e.g. if a user is in the person list screen, and clicks to add a new person,
     then fills in the details and clicks save, they are brought to the person detail screen for the person they just added. If
     the user then clicks back, they go back to the person list screen, not the edit screen).  
* Links and invites: there are _many_ different ways and paths that could be used from getting a link on the first device to
  opening a link on the second device (e.g. scan QR code using camera app, scan QR code using RESPECT app itself, send link
  via a messenger app and then open it on the second device, copy paste link on second device into other options, enter link
  screen, type school name then enter invite code, etc). It is not feasible to prototype all potential paths.
   * Once the user arrives at the destination on the second device. how they got there **does not affect behavior**.
   * When opening a link from another app, the [Onboarding](respect-lib-shared/src/commonMain/kotlin/world/respect/shared/viewmodel/onboarding/OnboardingViewModel.kt)
     screen will be shown if the user has not seen it and clicked the 'Get started' button on that screen before. If
     the user is opening a link, they will be taken to that link after clicking the 'Get started' button. If the user
     has already seen the Onboarding screen and clicked 'Get started', then the user will be taken directly to the link.
   * If the user did not have the RESPECT app installed, they will be redirected to Google Play with a referral url set.
     When the user installs the app and opens it for the first time, the user will be taken to that link destination (e.g.
     to accept an invite) after going through the onboarding screen, the same as if they already had the app installed and
     clicked the link (as outlined above). This is a ['Deferred Deep Link'](https://support.google.com/google-ads/answer/16420273?hl=en) (implemented using [GetDeferredDeepLinkUseCase.kt](respect-lib-shared/src/commonMain/kotlin/world/respect/shared/domain/navigation/deferreddeeplink/GetDeferredDeepLinkUseCase.kt)).


