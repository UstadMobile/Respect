General:

* Designs follow [Material3](https://m3.material.io/) guidelines (unless there is an absolute need / justification to not do so in a specific case)
* Designs _always_ follow patterns seen in other widely used apps 
* Where the on screen keyboard would likely cover textfields (e.g. an edit screen with more than 2 textfields), then the action button (e.g. next/done/save) should be in the top right
* Where a user's actions are saved to the database/server, the the action text should be __save__. Where the changes are not directly saved (e.g. when the user is taken
  another screen to edit a component (such as the filter in a report) then the text should be __done__.  
* Screens should be as intuitive as possible. Explicit text explanations of what to do next are a _last resort_ (e.g. as used with passkeys, as per
  Google's UX guidance because users are not familiar with them).

Final designs for development:

* Should be unambiguous to any reasonable developer (covering all reasonably forseeable scenarios). It must be clear what behavior is expected.
* Screens must be linked so that the devleoper can understand the flow.

