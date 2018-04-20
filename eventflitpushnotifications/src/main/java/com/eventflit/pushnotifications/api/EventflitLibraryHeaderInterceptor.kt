package com.eventflit.pushnotifications.api

import com.eventflit.pushnotifications.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class EventflitLibraryHeaderInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val newRequest = request.newBuilder()
        .addHeader("x-eventflit-library", "eventflit-push-notifications-android ${BuildConfig.VERSION_NAME}")
        .build()
    return chain.proceed(newRequest)
  }
}
