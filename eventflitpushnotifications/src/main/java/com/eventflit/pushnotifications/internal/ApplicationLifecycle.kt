package com.eventflit.pushnotifications.internal

import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.eventflit.pushnotifications.BuildConfig
import com.eventflit.pushnotifications.api.DeviceMetadata
import com.eventflit.pushnotifications.api.OperationCallback
import com.eventflit.pushnotifications.api.PushNotificationsAPI
import com.eventflit.pushnotifications.logging.Logger

class AppActivityLifecycleCallbacks: Application.ActivityLifecycleCallbacks {

  companion object {
    var startedCount = 0
    var stoppedCount = 0
    fun appInBackground(): Boolean = startedCount <= stoppedCount
  }

  override fun onActivityPaused(activity: Activity?) {
  }

  override fun onActivityResumed(activity: Activity?) {
  }

  override fun onActivityStarted(activity: Activity?) {
    Companion.startedCount += 1
  }

  override fun onActivityDestroyed(activity: Activity?) = Unit

  override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
  }

  override fun onActivityStopped(activity: Activity?) {
    Companion.stoppedCount +=1
  }

  override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
  }

}

class PushNotificationsInitProvider: ContentProvider() {
  private val log = Logger.get(this::class)

  override fun onCreate(): Boolean {
//    context.getSharedPreferences(
//            "com.eventflit.pushnotifications.PushNotificationsInstance", Context.MODE_PRIVATE
//    ).edit().clear().apply()

    val deviceStateStore = DeviceStateStore(context)

    deviceStateStore.appId?.let {
      val api = PushNotificationsAPI(it)

      api.deviceId = deviceStateStore.deviceId
      api.setSubscriptions(deviceStateStore.interestsSet, OperationCallback.noop)

      if (deviceStateStore.sdkVersion != BuildConfig.VERSION_NAME
          || deviceStateStore.osVersion != Build.VERSION.RELEASE) {
        val metadata = DeviceMetadata(BuildConfig.VERSION_NAME, Build.VERSION.RELEASE)
        api.setMetadata(metadata, operationCallback = object: OperationCallback {
          override fun onSuccess() {
            deviceStateStore.sdkVersion = BuildConfig.VERSION_NAME
            deviceStateStore.osVersion = Build.VERSION.RELEASE
          }

          override fun onFailure(t: Throwable) {
            log.w("Failed to persist metadata.", t)
          }
        })
      }
    }

    (context.applicationContext as? Application).apply {
      when(this) {
        is Application -> registerActivityLifecycleCallbacks(AppActivityLifecycleCallbacks())
        else -> log.w("Failed to register activity lifecycle callbacks. Notification delivery events might be incorrect.")
      }
    }

    return false
  }

  override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
  override fun insert(uri: Uri?, values: ContentValues?): Uri? = null
  override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
  override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int = 0
  override fun getType(uri: Uri?): String? = null
}
