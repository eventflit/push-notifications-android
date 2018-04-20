# Eventflit Push Notifications Android 

[![Twitter](https://img.shields.io/badge/twitter-@Eventflit-blue.svg?style=flat)](http://twitter.com/Eventflit)

This is the Android SDK for the [Push Notifications](https://eventflit.com/) service.

The SDK is written in Kotlin, but aimed to be as Java-friendly as possible

## Installation

### Update your project level gradle config

Add the Google Services classpath to the dependencies section of your project-level `build.gradle`:

```
buildscript {
    ...

    dependencies {
        // Add this line
        classpath 'com.google.gms:google-services:3.1.0'
    }
}
```

### Update your app level gradle config

```
dependencies {
    ...

    // Add these lines
    implementation 'com.google.firebase:firebase-messaging:11.8.0'
    implementation 'com.eventflit:push-notifications-android:0.1.0'
}

// Add this line to the end of the file
apply plugin: 'com.google.gms.google-services'
```

## Documentation

You can find our up-to-date documentation in [here](https://docs.eventflit.com/push-notifications/).

## Communication

- Found a bug? Please open an [issue](https://github.com/eventflit/push-notifications-android/issues).
- Have a feature request. Please open an [issue](https://github.com/eventflit/push-notifications-android/issues).
- If you want to contribute, please submit a [pull request](https://github.com/eventflit/push-notifications-android/pulls) (preferably with some tests).

## License

Push Notifications is released under the MIT license. See [LICENSE](https://github.com/eventflit/push-notifications-android/blob/master/LICENSE) for details.
