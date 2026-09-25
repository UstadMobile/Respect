# lib-xapi-nanohttpd guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview:

This module contains a NanoHTTPD server that can be embedded and run locally in the
mobile app to provide a localhost Experience API (xAPI) http server that works offline. 

Experience API enabled content that is loaded into a webview can then send xAPI requests to
the embedded server. A localhost URL will be provided as the endpoint parameter using the 
Rustici launch method when xAPI HTML content is launched by the mobile app.

Request paths will follow the following format:

/e/(upstream-xapi-server-url-double-encoded)/xapi-resource-name

The upstream xapi server URL is passed to XapiResourceProvider to get an XapiResource
implementation (e.g. offline-first repository or database backed).


