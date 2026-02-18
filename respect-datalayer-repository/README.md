# RESPECT-Datalayer-Repository

The repository datalayer implementations provide an offline-first way to access the datalayer for
clients that support a local datalayer (e.g. mobile/desktop apps). This improves the user experience 
by:
* Allowing the user to access data whilst offline when there is no connectivity
* Enabling screens (based on a ViewModel) to display local data _immediately_ and check for updated
  data in the background, without requiring the user to wait.
* Allowing data to be saved locally immediately, online or offline, without requiring the user to 
  wait. Changes are queued to be sent to the server.

There are generally three patterns used by the repository:
* **Read as flow**: the repository returns a flow from the local datalayer. The remote flow checks for
  updates in the background as needed concurrently. If new data is received, it is inserted into the
  local datalayer. Inserting the data into the local database invalidates the flow and Room 
  automatically emits an update.
* **Read as single value** (suspended function): the repository tries to make an http request if a 
  connection is available. If data is received from the server, it is inserted into the local 
  database. The repository then returns the local value.
* **Write**: the data is saved in the local datalayer and enqueues a job to save it in the remote 
  datalayer as soon as connectivity is available.

#### Offline-first conceptual notes:

Data is generally one of three types:

* Immutable (never changes): e.g. Xapi Statements
* Read-only (can change, but changes are never written by the RESPECT app): e.g. OPDS feeds provided
  provided by _RESPECT Compatible_ apps.
* Mutable entities (can change, and changes may be written by the RESPECT app): e.g. RESPECT's own
  APIs for person/enrollment management, xAPI's state, activity profile, and agent profiles, etc.

**Getting entities modified since last check**:

In a multi-node system, especially one where multiple nodes can be offline for extended periods of
time or where there may be peer-to-peer communication, the _stored_ time and the _last-modified_
time are not the same thing.

The last-modified time is the time that a change actually happened (e.g. when a user clicked save
on making a change on their device). The _stored_ time is unique to a specific device, and is when the
entity was actually stored on that specific device. The last-modified and stored time on the
device where the change took place will be (almost) the same. However the stored time on the server
will be when the server received it. The stored time on another phone will be the time that the
device received that version of the entity (whether that was directly from a trusted server or via
any peer-to-peer system).

A common need for an efficient client is to get a list of changes since the client last checked.
This **must** be based on the stored time, not the last-modified time (just because an entity had
been modified does not mean that the change has reached the server at the same time).

The server (whether it is a top level server or peer) SHOULD add an X-Consistent-Through header
(as used in [xAPI](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#requirements-4)). If a client has received a response with X-Consistent-Through it can then
use this as a parameter for the next response adding it to a query parameter where
supported by an API (e.g. xAPI statements resource).
> with a value of the timestamp for which all Statements that have or will have a "stored" property before that time are known with reasonable certainty to be available for retrieval

Generally a Last-Modified HTTP header SHOULD actually be the most recent stored time of any data 
included in the response: because this is the most recent time the response of this server actually 
changed.

When permissions change this can lead to a situation where a client using stored since parameter would
not receive new entities because they were stored before the stored-since, but the client
only received permission to access them thereafter. Where permissions have changed after 
stored since parameter the server MUST ignore the stored since parameter (by definition permission 
changes invalidates any previous Consistent-Through header).

The stored since parameter SHOULD be used for efficient periodic syncs (where a client checks
for new relevant data periodically or in response to an event). It SHOULD NOT be used for data pull
requests that typically originate from the UI (e.g. where the client pulls data because the user is
visiting a particular screen). If-None-Match etags / If-Modified-Since should be used instead. 
Consider a paged list: if the since parameter is used it becomes almost impossible to align local
and remote data.

**Handling deletions**

In an offline-first system data is, by nature, distributed amongst various nodes. This makes 
handling deletion more complicated. Simply deleting from any node's database is insufficient, even
if from the server, the data would still be present on other nodes.

Each entity has a status field (see StatusEnum.kt). When data is to be deleted, the status must be
changed to to_be_deleted (this change should be saved using the _store_ function the same as any 
other update). Any personal data should be removed at this point (set status to_be_deleted and set
any personal data fields to null).

It should then generally be excluding from db query results. When the repository pulls remote updates
from the server, it will set includeDeleted=true to ensure that any deletions are pulled down so 
they take effect locally.

The actual deletion will happen after a period of time (e.g. 10 days) has passed. Each node will
periodically search for entities with the status to_be_deleted where the time since last modified 
exceeds the period (e.g. 10 days), and then actually delete those entities.
