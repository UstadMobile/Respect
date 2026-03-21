* This datalayer is implemented using Room Database (Multiplatform)
* There are two databases: RespectSchoolDatabase for school-level data and RespectAppDatabase for
  app-wide data (as per respect-datalayer itself).
* For each model there is an adapter file in the relevant adapters package that will convert the
  model to/from entities.
* Each entity class that directly represents a model class should have a uidNumber field to store a 
  has of the string UID (or any other scheme implemented using the UidNumberMapper interface).
* Each Entity is named in the form of ModelNameEntity
* Entity classes property names should be prefixed to avoid name clashes in queries e.g. 
  AssignmentEntity fields are prefixed ae so fieldnames are aeUid, aeLastModified, etc.
* When a model class uses an enum type, then use the same enum type on the entity class. Room 
  TypeConverters to convert to/from database types (such as those found in ```src/commonMain/kotlin/world/respect/datalayer/db/schooldirectory```
  ```respect-datalayer-db/src/commonMain/kotlin/world/respect/datalayer/db/shared/SharedConverters.kt```) 
  are used to convert Enum types to/from an Int.
* Where a model has list fields create a 1:many join and use multiple entities. Create a class using
  Room's @Relation annotation. Joined entities (e.g. the main side) should use autoIncrement primary
  keys. When updated data is stored, old versions of the joined entity are deleted and new entities
  are inserted.
* Each DAO must implement the Local DataSource interface (e.g. ModelNameDataSourceLocal).
* Data that is given the status TO_BE_DELETED will be deleted by a cron job / timed WorkManager task. 
  Do not generate delete queries for an entity that directly represents a model class.
