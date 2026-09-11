# lib-xapi-core guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module contains : 

* **Experience API (xAPI) data models Kotlin classes** with serialization support using kotlinx 
  serialization.
* **Experience API (xAPI) resource interface classes** based on the xAPI communication specification
  where parameters are as per the xAPI specification
* **Utility extension functions** simple shorthand functions like 
  `fun XapiActivity.definitionOrBlank(): XapiActivityDefinition`.

## Module guidance

* The module is based on the xAPI 1.0.3 specification as per 
  [https://github.com/adlnet/xAPI-Spec/](https://github.com/adlnet/xAPI-Spec/). 
* The data models and resource interface classes MUST reflect the xAPI specification EXACTLY and 
  comments must link to the relevant parts of the specification.
