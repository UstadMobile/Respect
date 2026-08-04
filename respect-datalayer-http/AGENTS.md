* This module contains the http client implementation of the datalayer
* Always follow the patterns as seen in other DataSource implementations found this module.
* All implementations in the ```world.respect.datalayer.http.school``` package should take 
  the school Url, SchoolDirectoryEntryDataSource, KTOR HttpClient, and ExtendedDataSourceValidationHelper
  (nullable) as constructor parameters as per ```src/commonMain/kotlin/world/respect/datalayer/http/school/AssignmentDataSourceHttp.kt```.
* The HTTP server module (in the respect-server module) has an HTTP GET and POST endpoint. The same
  GET endpoint is used for all read operations. The POST endpoint is used to store/update data.
* The HTTP get operation should always use the model datasource GetListParams function. Create a 
  function ModelNameDataSource.GetListParams.urlWithParams as per
  ```src/commonMain/kotlin/world/respect/datalayer/http/school/AssignmentDataSourceHttp.kt``` to 
  generate the get URL.
