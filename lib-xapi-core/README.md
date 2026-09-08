# lib-xapi-model

This is a standalone module that contains only model classes for xAPI spec objects, resources, and
Kotlinx serialization annotations and custom serializers as needed to serialize/deserialize them
as per the spec.

It is intended to be used both within the launcher app and by third party apps when 
sending/receiving xAPI data.

Note on canonical definitions: when a statement is transferred over the network by a repository,
it will _always_ be transferred using the Exact format (because Xapi statements are immutable).

Canonical definitions will be updated in the local database on the basis of statements it has seen.
This can lead to a situation where the canonical definition will be different locally when using
an offline-first access on demand approach (e.g. when a statement with the latest definition
update has not yet been accessed locally).
