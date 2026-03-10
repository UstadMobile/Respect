# RESPECT

RESPECT is an open source digital library for EdTech apps. It makes it easier for educators to 
discover and use high-quality apps with a single account in all settings, while giving developers 
the platform they need to grow their impact globally.

[<img src="https://devserver3.ustadmobile.com/jenkins/job/RESPECT-Primary/badge/icon?subject=Build">](https://devserver3.ustadmobile.com/jenkins/job/RESPECT-Primary)

[<img src="https://devserver3.ustadmobile.com/jenkins/job/RESPECT-end-to-end//badge/icon?subject=End-To-End-Tests">](https://devserver3.ustadmobile.com/jenkins/job/RESPECT-end-to-end)

## Development environment setup:

These instructions are intended for developers who wish to build/run from source code. Development
tested on Ubuntu Linux, however it should work on Windows, other Linux versions, or MacOS. 

This is a Kotlin Multiplatform project. This repository contains the Android app and
backend server source code in its modules. Android Studio is the development environment for the
entire project. 

*  __Step 1: Download and install Android Studio__: If you don't already have the latest version, download
   from [https://developer.android.com/studio](https://developer.android.com/studio).

* __Step 2: Install dependencies__
    * JDK17 or JDK21

Ubuntu/Debian Linux:
```
sudo apt-get install openjdk-21-jdk
```

Windows:
Download and install the Microsoft OpenJDK build from 
[https://learn.microsoft.com/en-us/java/openjdk/install#install-on-windows](https://learn.microsoft.com/en-us/java/openjdk/install#install-on-windows).

* __Step 3: Import the project in Android Studio__: Select File, New, Project from Version Control. Enter
  https://github.com/UstadMobile/Respect.git and wait for the project to import.

* __Step 4: Run the server__: Run the server using Gradle:

Run the server from source using Gradle:
```
./gradlew respect-server:run
```
_Note: On the windows command line the ./ should be omitted_

* __Step 5: Add a [school](ARCHITECTURE.md#schools)__ - each school has its own users, classes, etc.
  Each school instance has its own database (e.g. database for school1, school2, etc).

  RESPECT supports virtual hosting enabling multiple schools to run within a single JVM instance, eg
  as school1.example.org, school2.example.org etc.

e.g.
```
./gradlew respect-server:run --args='addschool --url http://10.1.2.3:8098/ --name devschool --adminpassword secret' 
```
Note: localhost _won't_ work on Android emulators and devices because localhost refers to the 
emulator/device itself _not_ the PC running on the server.

To see all available command line options (including database options etc):
```
./gradlew respect-server:run --args='addschool --help'
```

Note: in order for the search by school name to work you must add your server to the app directory
list (default or local)
(e.g. http://10.1.2.3:8098/ as above) in [directories](respect-lib-shared/src/androidMain/resources/directories)

* __Step 6: Build/run and Android app__: In Android Studio use the run/debug button to run the 
 ```respect-app-compose``` module. See [respect-app-compose](respect-app-compose/) for further
 details on running via the command line etc.

## Community

Join our [Community Slack Space](https://join.slack.com/t/respectdevelopers/shared_invite/zt-3h04mk3r6-SO1hBLbn0yj5kczPS7q2eg).

## Build environment variables

The following environment variables can be set:

```
RESPECT_DEFAULT_APPLIST - the default list of RESPECT Compatible app manifest URLs e.g. https://respect.world/respect-ds/manifestlist.json
```

## Legal and license

Copyright 2024-2025 UstadMobile FZ-LLC. This code is substantially derived from [UstadMobile](https://www.github.com/UstadMobile/UstadMobile/).
Documentation: [CC-BY](https://creativecommons.org/licenses/by/4.0/) license.
Code and all other works: [AGPLv3](LICENSE) license.

‘RESPECT™’ and ‘RESPECT compatible™’ are trademarks of the Spix Foundation.
‘OneRoster®’ and ‘LTI®’ are registered trademarks of 1EdTech Consortium Inc.
All other trademarks and registered trademarks are the properties of their respective owners.
