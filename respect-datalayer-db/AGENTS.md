# RESPECT respect-datalayer-db guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview
This is an implementation of the [respect-datalayer module](../respect-datalayer) 
using Room Multiplatform (based on SQLite). This module is used by the mobile app implementations
to provide a local offline-first data source and by the server module.

You should read [respect-datalayer AGENTS.md](../respect-datalayer/AGENTS.md) before working with
code in this module.

* There are two databases: RespectSchoolDatabase for school-level data and RespectAppDatabase for
  app-wide data (as per respect-datalayer itself).
* For each model there is an adapter file in the relevant adapters package that will convert the
  model to/from database entities.
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
* Each DataSource in this module must implement ModelNameLocalDataSource.
