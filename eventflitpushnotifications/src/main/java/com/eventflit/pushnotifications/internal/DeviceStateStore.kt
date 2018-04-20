package com.eventflit.pushnotifications.internal

import android.content.Context

class DeviceStateStore(context: Context) {
  private val preferencesDeviceIdKey = "deviceId"
  private val preferencesFCMTokenKey = "fcmToken"
  private val preferencesAppIdKey = "appId"
  private val preferencesOSVersionKey = "osVersion"
  private val preferencesSDKVersionKey = "sdkVersion"
  private val preferencesInterestsSetKey = "interests"

  private val preferences = context.getSharedPreferences(
    "com.eventflit.pushnotifications.PushNotificationsInstance", Context.MODE_PRIVATE)

  var appId: String?
    get() = preferences.getString(preferencesAppIdKey, null)
    set(value) = preferences.edit().putString(preferencesAppIdKey, value).apply()

  var deviceId: String?
    get() = preferences.getString(preferencesDeviceIdKey, null)
    set(value) = preferences.edit().putString(preferencesDeviceIdKey, value).apply()

  var FCMToken: String?
    get() = preferences.getString(preferencesFCMTokenKey, null)
    set(value) = preferences.edit().putString(preferencesFCMTokenKey, value).apply()

  var osVersion: String?
    get() = preferences.getString(preferencesOSVersionKey, null)
    set(value) = preferences.edit().putString(preferencesOSVersionKey, value).apply()

  var sdkVersion: String?
    get() = preferences.getString(preferencesSDKVersionKey, null)
    set(value) = preferences.edit().putString(preferencesSDKVersionKey, value).apply()

  var interestsSet: MutableSet<String>
    get() = preferences.getStringSet(preferencesInterestsSetKey, mutableSetOf<String>())
    set(value) = preferences.edit().putStringSet(preferencesInterestsSetKey, value).apply()
}
