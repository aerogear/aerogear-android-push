# AeroGear Android Push [![Build Status](https://travis-ci.org/aerogear/aerogear-android-push.png)](https://travis-ci.org/aerogear/aerogear-android-push)

AeroGear's Android libraries were built as jar, apklib and aar using [Maven](http://maven.apache.org/) and [android-maven-plugin](https://github.com/jayway/maven-android-plugin). The project follows the standard Android project layout as opposed to the standard Maven layout so sources will be in /src instead of /src/main/java and can be imported directly into IDE as an Android project.

## Push

AeroGear for Android provides support for integrating with push. Currently only using Google’s Cloud Messaging (GCM) with the [AeroGear UnifiedPush Server](https://github.com/aerogear/aerogear-unifiedpush-server) is supported, but we are planning to add support for Mozilla’s Simple Push, MQTT, and standalone GCM soon.

## Building

Please take a look of [step by step on our website](http://aerogear.org/docs/guides/aerogear-android/how-to-build-aerogear-android/)

## Usage

There are two supported ways of developing apps using AeroGear for Android: Android Studio and Maven.

### Android Studio

Add to your application's `build.gradle` file

```
dependencies {
    compile 'com.google.android.gms:play-services:+'
    compile 'org.jboss.aerogear:aerogear-android-push:1.0.0@aar'
    compile 'com.google.guava:guava:13.0.1'
    compile 'com.google.code.gson:gson:2.2.2'
}
```

### Maven

Include the following dependencies in your project's `pom.xml`


```
<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-push</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
  <type>jar</type>
</dependency>

<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-push</artifactId>
  <version>1.0.0</version>
  <type>apklib</type>
</dependency>
```

## Documentation

For more details about the current release, please consult [our documentation](http://aerogear.org/docs/guides/aerogear-android/).

## Demo apps

Take a look in our demo apps

* [AeroGear Push HelloWorld](https://github.com/aerogear/aerogear-push-helloworld)
* [AeroGear Push Quickstarts](https://github.com/aerogear/aerogear-push-quickstarts)

## Development

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

