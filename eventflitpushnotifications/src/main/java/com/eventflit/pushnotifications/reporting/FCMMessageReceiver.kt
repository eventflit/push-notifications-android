package com.eventflit.pushnotifications.reporting

import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver
import com.firebase.jobdispatcher.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.eventflit.pushnotifications.featureflags.FeatureFlag
import com.eventflit.pushnotifications.featureflags.FeatureFlagManager
import com.eventflit.pushnotifications.internal.AppActivityLifecycleCallbacks
import com.eventflit.pushnotifications.internal.DeviceStateStore
import com.eventflit.pushnotifications.logging.Logger
import com.eventflit.pushnotifications.reporting.api.DeliveryEvent
import com.eventflit.pushnotifications.reporting.api.ReportEvent
import com.eventflit.pushnotifications.reporting.api.ReportEventType

class FCMMessageReceiver : WakefulBroadcastReceiver() {
  private val gson = Gson()
  private val log = Logger.get(this::class)

  override fun onReceive(context: Context?, intent: Intent?) {
    if (!FeatureFlagManager.isEnabled(FeatureFlag.DELIVERY_TRACKING)) {
      log.i("Delivery tracking flag is disabled. Skipping.")
      return
    }

    intent?.getStringExtra("eventflit")?.let { eventflitDataJson ->
      try {
        val eventflitData = gson.fromJson(eventflitDataJson, EventflitMetadata::class.java)
        log.i("Got a valid eventflit message.")

        if (context == null) {
          log.w("Failed to get device ID (no context) - Skipping delivery tracking.")
          return
        }

        val deviceId = DeviceStateStore(context).deviceId
        if (deviceId == null) {
          log.w("Failed to get device ID (device ID not stored) - Skipping delivery tracking.")
          return
        }

        val reportEvent = DeliveryEvent(
          publishId = eventflitData.publishId,
          deviceId =   deviceId,
          timestampSecs = Math.round(System.currentTimeMillis() / 1000.0),
          appInBackground = AppActivityLifecycleCallbacks.appInBackground(),
          hasDisplayableContent = eventflitData.hasDisplayableContent,
          hasData = eventflitData.hasData
        )

        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
        val job = dispatcher.newJobBuilder()
          .setService(ReportingJobService::class.java)
          .setTag("eventflit.delivered.publishId=${eventflitData.publishId}")
          .setConstraints(Constraint.ON_ANY_NETWORK)
          .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
          .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
          .setExtras(ReportingJobService.toBundle(reportEvent))
          .build()

        dispatcher.mustSchedule(job)
      } catch (_: JsonSyntaxException) {
        // TODO: Add client-side reporting
      }
    }
  }
}
