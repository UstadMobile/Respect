# RESPECT respect-datalayer-db guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview
This is an implementation of the [respect-datalayer module](../respect-datalayer) 
using Room Multiplatform (based on SQLite). This module is used by the mobile app implementations
to provide a local offline-first data source and by the server module.

You should read [respect-datalayer AGENTS.md](../respect-datalayer/AGENTS.md) before working with
code in this module.

## Entity guidance

Entities are Kotlin data classes with the Room @Entity annotation e.g.

FooEntity.kt:
```kotlin
@Entity(tableName = "foo")
data class FooEntity(
    val id: String = Uuid.random().toString(),
    
    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnINfo(name = "last_name")
    val lastName: String,
)
```

Create a separate adapter to convert form the model class to the entity class(es) and vice versa for 
file for each model: e.g. FooAdapter:
```kotlin
fun Foo.toEntity(): FooEntity {
    return FooEntity(
        id = uid,
        firstName = firstName,
        lastName = lastName,
    )
}

fun FooEntity.toModel(): Foo {
    return Foo(
        uid = id,
        firstName = firstName,
        lastName = lastName,
    )
}
```

Where a model has list fields or maps 1:Many or Many:Many joins may be required. In this case there
will be multiple entities. For example:

```kotlin
data class FooWithMultipleNames(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val previousKnownAliases: List<String>,
)
```

This should use a join class. In addition to `FooEntity.kt` add `FooPreviousKnownAliasJoinEntity.kt`:
```kotlin
@Entity(tableName = "foo_previous_known_alias_join")
data class FooPreviousKnownAliasJoinEntity(
    @PrimaryKey(autoIncrement = true)
    val id: Int = 0,
    
    //The foreign key that links to FooEntity
    @ColumnInfo(name = "foo_id")
    val fooId: String,
    
    @ColumnInfo(name = "previous_known_alias")
    val previousKnownAlias: String,
)
```

## General guidance
* There are two databases: RespectSchoolDatabase for school-level data and RespectAppDatabase for
  app-wide data (as per respect-datalayer itself).
* Each Entity is named in the form of ModelNameEntity
* When a model class uses an enum type, then use the same enum type on the entity class. Room 
  TypeConverters to convert to/from database types (such as those found in ```src/commonMain/kotlin/world/respect/datalayer/db/schooldirectory```
  ```respect-datalayer-db/src/commonMain/kotlin/world/respect/datalayer/db/shared/SharedConverters.kt```) 
  are used to convert Enum types to/from an Int.
* Where a model has list fields create a 1:many join and use multiple entities. Create a class using
  Room's @Relation annotation where possible. Joined entities (e.g. the main side) should use 
  autoIncrement primary
  keys. When updated data is stored, old versions of the joined entity are deleted and new entities
  are inserted.
* Each DataSource in this module must implement ModelNameLocalDataSource.
