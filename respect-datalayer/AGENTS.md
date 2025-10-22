* The datalayer is split into two parts: SchoolDataSource for school-level data (users, student 
  progress, etc) and RespectAppDataSource for app-wide data.
* Any writable model class should implement the ```ModelWithTimes``` interface such that it can be
  synced
* Models normally have a string uid (required as this UID may come from an external system). 
* All models must be serializable using kotlinx Serialization.
* Data is deleted by changing the status to TO_BE_DELETED instead of it being directly deleted. This
  allows sync'd systems to understand that a given piece of data has been deleted.
