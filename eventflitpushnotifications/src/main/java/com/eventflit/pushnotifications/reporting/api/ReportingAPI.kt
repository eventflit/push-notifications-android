package com.eventflit.pushnotifications.reporting.api

import com.google.gson.Gson
import com.eventflit.pushnotifications.api.EventflitLibraryHeaderInterceptor
import com.eventflit.pushnotifications.api.OperationCallback
import com.eventflit.pushnotifications.reporting.ReportingJobService
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReportingAPI(private val appId: String) {
  private val baseUrl = "https://push.eventflit.com/reporting/app/$appId/"

  private val gson = Gson()

  private val client =
    OkHttpClient.Builder()
      .addInterceptor(EventflitLibraryHeaderInterceptor())
      .build()

  private val service =
    Retrofit.Builder()
      .baseUrl(baseUrl)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .client(client)
      .build()
      .create(ReportingService::class.java)

  fun submit(reportEvent: ReportEvent, operationCallback: OperationCallback) {
    val callback = object : Callback<Void> {
      override fun onResponse(call: Call<Void>?, response: Response<Void>) {
        when {
          response.code() in 200..299 -> {
            operationCallback.onSuccess()
          }
          response.code() >= 500 ->
            onFailure(call, RuntimeException("Failed to submit reporting event"))
          else ->
            onFailure(call, UnrecoverableRuntimeException("Failed to submit reporting event"))
        }
      }

      override fun onFailure(call: Call<Void>?, t: Throwable) {
        operationCallback.onFailure(t)
      }
    }

    service.submit(
      reportingRequest = reportEvent
    ).enqueue(callback)
  }
}
