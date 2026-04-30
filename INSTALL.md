# Installing Respect on your own server

The Respect server runs as a standalone Java application (powered by KTOR and Netty) It. can be
connected to an SQLite database (one database per school). It is recommended to use Apache or Nginx as a proxy.
The server should run fine on any platform that supports JVM (Linux, Windows, Mac, etc), but it has
been most extensively tested on Ubuntu Linux.

## Quickstart

### 1. Get respect-server.zip distribution

Download respect-server.zip from [GitHub releases](https://www.github.com/UstadMobile/Respect/releases) or
build the respect-server module source as per the __Production build__ procedure in 
[respect-server/README.md](respect-server/README.md). If you want to use the Android app you'll
need to download the APK file from [GitHub releases](https://www.github.com/UstadMobile/Respect/releases) or
build the APK from source as per the procedure in [respect-app-compose/README.md](app-android/README.md), 
or install from Google Play.

### 2. Install server requirements:

The server requires JDK21+. The __java__ command should be in the PATH or the JAVA_HOME variable should
be set (this is done automatically by default when you install Java from an installer package e.g. 
using apt-get on Ubuntu or MSI/EXE for Windows).

On Ubuntu 23.10+:

Install required packages:
```
apt-get install openjdk-21-jdk
```

Note: if you have other Java versions, make sure you run the server jar using JDK21+. You can use 
``sudo update-alternatives --config java`` to set the default java version to run.

On Windows:
* Download and install Java (JDK21+) if not already installed e.g. from [https://learn.microsoft.com/en-us/java/openjdk/download](https://learn.microsoft.com/en-us/java/openjdk/download) 

### 3. Unzip respect-server.zip and start server

Unzip respect-server.zip .

You can run the server directly:
```
$ unzip-path/bin/respect-server
```
_Or_ run as a system service on Linux (automatically starts on boot). Set the paths and username in 
systemd/respect-server.service and then run:
```
$ cp unzip-path/systemd/respect-server.service /etc/systemd/system/
$ sudo systemctl daemon-reload
$ sudo systemctl start respect-server
$ sudo systemctl enable respect-server
# Check status
$ sudo systemctl status respect-server
```

### 4. Add one or more schools

```
./gradlew respect-server:run --args='addschool --url http://10.1.2.3:8098/ --name devschool --adminpassword secret' 
```

Where --url is the public URL on which the server can be reached. In production this must use
https and a reverse proxy (e.g. Apache or Nginx) should be used to run a reverse proxy (see below).

Note: localhost _won't_ work on Android emulators and devices because localhost refers to the 
emulator/device itself _not_ the PC running on the server.

To see all available command line options (including database options etc):
```
./gradlew respect-server:run --args='addschool --help'
```

### 4. Install the APK and connect to server

Drag and drop the APK onto the emulator, or use the command:

```
adb install app-android-launcher-release.apk
```

When the app prompts you for a link, you should enter the link using the IP address of the server
e.g. http://192.168.0.10:8087/ where 192.168.0.10 is the IP address of the PC running the server.
Using localhost on an Android device or emulator will **NOT** work. If you are using an Android
emulator, you can use 10.0.2.2 which always points to the Android emulator host device.

## Customize server configuration (optional)

You may create a update the configuration in respect-server.conf to set the database, directory where
server data is stored, and other options. 

## Production recommendations

### Use a reverse proxy

Use an HTTP server such as Apache or Nginx with a reverse proxy. Apache or Nginx
should be used to provide https support e.g. as per [Apache Reverse Proxy Guide](https://httpd.apache.org/docs/2.4/howto/reverse_proxy.html).
The [Forwarded](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded) header must include the protocol (e.g. http or https) or 
the [X-Forwarded-Proto](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto) 
header must be set. 

Apache example:
```
# AllowEncodedSlashes must be enabled, otherwise blob paths will not work
AllowEncodedSlashes On

#Nocanon is required to ensure that encoding in paths is not altered
ProxyPass / http://localhost:8087/ nocanon
ProxyPassReverse / http://localhost:8087/
SSLProxyEngine On
ProxyPreserveHost On
RequestHeader set X-Forwarded-Proto https
```

If using virtual hosting, then set ServerName and ServerAlias e.g.

```
ServerName example.org
ServerAlias *.example.org
```

Enable required Apache modules:
```
a2enmod proxy headers
```
Recommended:
```
a2enmod deflate
```
Apache mod deflate will compress javascripts, JSON data, etc sent over the network. 

Run the server using a script on startup or use the screen command.

### Autostart using systemd

This should be done using SystemD on most Linux distributions (including Ubuntu). Modify the paths
and user (if needed) in systemd/respect-server.service and then install the service:

```
$ cp unzip-path/systemd/respect-server.service /etc/systemd/system/
$ sudo systemctl daemon-reload
$ sudo systemctl start respect-server
$ sudo systemctl enable respect-server
# Check status
$ sudo systemctl status respect-server
```

