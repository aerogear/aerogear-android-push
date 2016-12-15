# AeroGear Android Push

[![Maintenance](https://img.shields.io/maintenance/yes/2016.svg)](http://github.com/aerogear/aerogear-android-push)
[![Travis](https://img.shields.io/travis/aerogear/aerogear-android-push.svg)](http://travis-ci.org/aerogear/aerogear-android-push)
[![License](https://img.shields.io/badge/-Apache%202.0-blue.svg)](https://opensource.org/s/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.jboss.aerogear/aerogear-android-push.svg)](http://search.maven.org/#search%7Cga%7C1%7Caerogear-android-push)
[![Javadocs](http://www.javadoc.io/badge/org.jboss.aerogear/aerogear-android-push.svg?color=blue)](http://www.javadoc.io/doc/org.jboss.aerogear/aerogear-android-push)

AeroGear's Android libraries were built as jar and aar packages using [Maven](http://maven.apache.org/) and the [android-maven-plugin](https://github.com/jayway/maven-android-plugin). The project follows the standard Maven layout so it can be imported directly into most IDEs as a Maven project.

## Push

AeroGear Android Push provides support for integrating with push. Currently only using Firebase Cloud Messaging (FCM) with the [AeroGear UnifiedPush Server](https://github.com/aerogear/aerogear-unifiedpush-server) is supported, but we are planning to add support for Mozilla’s Simple Push, MQTT, and standalone GCM soon.

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Maven  |
| Documentation:  | http://aerogear.org/docs/guides/aerogear-android/  |
| Issue tracker:  | https://issues.jboss.org/browse/AGDROID  |
| Mailing lists:  | [aerogear-users](http://aerogear-users.1116366.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-users))  |
|                 | [aerogear-dev](http://aerogear-dev.1069024.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-dev))  |

## Building

Please take a look at the [step by step guide](http://aerogear.org/docs/guides/aerogear-android/how-to-build-aerogear-android/) on our website.

*The following dependencies are required to build this project:*

* [aerogear-android-core](http://github.com/aerogear/aerogear-android-core) 
* [aerogear-android-pipe](http://github.com/aerogear/aerogear-android-pipe) 

## Usage

There are two supported ways of developing apps using AeroGear for Android: Android Studio and Maven.

### Android Studio

Add to your application's `build.gradle` file

```groovy
dependencies {
    compile 'com.google.firebase:firebase-messaging:9.8.0'
    compile 'org.jboss.aerogear:aerogear-android-push:4.1.0'
}
```

### Maven

Include the following dependencies in your project's `pom.xml`

```xml
<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-push</artifactId>
  <version>4.1.0</version>
  <scope>provided</scope>
  <type>jar</type>
</dependency>

<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-push</artifactId>
  <version>4.1.0</version>
  <type>aar</type>
</dependency>
```

## Documentation

For more details about the current release, please consult [our documentation](http://aerogear.org/android/).

## Demo apps

Take a look in our demo apps

* [Hello Push](https://github.com/aerogear/aerogear-android-cookbook/tree/master/HelloPush)
* [AeroDoc](https://github.com/aerogear/aerogear-android-cookbook/tree/master/AeroDoc)

## Development

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGDROID) with some steps to reproduce it.

