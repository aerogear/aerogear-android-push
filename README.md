aerogear-android-push
================

AeroGear for Android provides support for integrating with push. Currently only using Google’s Cloud Messaging (GCM) with the [AeroGear Unified Push Server](https://github.com/aerogear/aerogear-unifiedpush-server) is supported, but we are planning to add support for Mozilla’s Simple Push, MQTT, and standalone GCM soon.

## Build

Please take a look of [step by step on our website](http://aerogear.org/docs/guides/aerogear-android/how-to-build-aerogear-android/)

## Usage

### Referencing a library project

Download [apklib from Maven central](http://search.maven.org/#search%7Cga%7C1%7Caerogear-android) and follow the [Google Android document](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject)

### Maven

```
<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-push</artifactId>
  <version>0.1-SNAPSHOT</version>
  <scope>provided</scope>
  <type>jar</type>
</dependency>

<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-push</artifactId>
  <version>0.1-SNAPSHOT</version>
  <type>apklib</type>
</dependency>
```

### Gradle (as experimental)
```
dependencies {
  compile 'org.jboss.aerogear:aerogear-android-push:0.1-SNAPSHOT@aar'
}
```

## Demo and Documentation

Take a look in our example apps and docs about our features

| Feature / Doc  |  Example |
|:--------------:|:--------:|
| [Push](http://aerogear.org/docs/guides/aerogear-android/push/) | [Push HelloWorld](https://github.com/aerogear/aerogear-push-helloworld) |


If you are having troubles feel free to contact us via IRC #aerogear or our mailing list aerogear-dev@lists.jboss.org.