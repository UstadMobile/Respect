* The datalayer is split into two parts: SchoolDataSource for school-level data (users, student 
  progress, etc) and RespectAppDataSource for app-wide data.
* Any writable model class should implement the ```ModelWithTimes``` interface such that it can be
  synced
* Models normally have a string uid (required as this UID may come from an external system). 
* All models must be serializable using kotlinx Serialization.
* Data is deleted by changing the status to TO_BE_DELETED instead of it being directly deleted. This
  allows sync'd systems to understand that a given piece of data has been deleted.
* Always add a GetListParams data class to the DataSource interface. This is used as a parameter for
  list functions (e.g. list, listAsFlow, listAsPagingSource etc). The GetListParams function should
  always have a fromParams companion function that accepts a single ```StringValues``` parameter that will 
  return a GetListParams data class based on the parameters represented by the StringValues object.
  The Http client will add the GetListParams to the URL query parameters and the HTTP server will 
  use the fromParams function to convert the parameters back into a data class.
