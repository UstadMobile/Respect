
* This is a Kotlin Multiplatform Project using Jetpack compose
* The NavHost with composable routes in ```respect-app-compose/src/commonMain/kotlin/world/respect/app/app/AppNavHost.kt```
* The datalayer is based on the ```respect-datalayer``` module. It is an offline first data layer. The
  local (Room Database) implementation is in the module ```respect-datalayer-db```. The http 
  implementation is in ```respect-datalayer-http```. The offline first repository is in 
  ```respect-datalayer-repository```.
* The main Android activity is ```respect-app-compose/src/androidMain/kotlin/world/respect/MainActivity.kt```.
* Use Koin for dependency injection.
* ViewModels are in ```respect-lib-shared/src/commonMain/kotlin/world/respect/shared/viewmodel```
* Always follow patterns shown in the project itself.
* Follow official architecture recommendations, including use of a layered architecture. 
* Always put Business logic in the ViewModel or a domain layer UseCase (where the same business 
  logic may be used by multiple ViewModels).
