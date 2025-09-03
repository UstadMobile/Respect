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
use this as a parameter for the next response by either a) adding it to a query parameter where
supported by an API (e.g. xAPI statements resource) or b) adding an X-Stored-Since header where
supported by an API (e.g. RESPECT extensions).
> with a value of the timestamp for which all Statements that have or will have a "stored" property before that time are known with reasonable certainty to be available for retrieval

Where a server or API does not support or recognize the difference between a stored time and a
last-modified time, a "quirks mode" approach can be used where the last-modified header is set using
the stored time. This works acceptably provided that any downstream client is communicating with
only one server API endpoint (e.g. a RESPECT Compatible app on the device that communicates only
with the RESPECT app, not directly with the online API endpoint).
