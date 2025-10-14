# respect-lib-shared-se

Broadly speaking RESPECT will run on two different classes of clients: 'standard edition' eg. mobile 
apps, desktop apps, server side, etc (all of which have a local database and file storage) and a
web client (which does not have a local database or file storage).

Use Case implementations that require the use of the database and/or filesystem but aren't specific
to Android or JVM etc need to be in a separate module (we must use the Kotlin multiplatform default
layout which does not support Android-JVM sharing).



