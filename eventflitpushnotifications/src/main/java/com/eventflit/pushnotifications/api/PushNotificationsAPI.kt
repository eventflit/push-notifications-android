package com.eventflit.pushnotifications.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.eventflit.pushnotifications.BuildConfig
import com.eventflit.pushnotifications.api.retrofit2.RequestCallbackWithExpBackoff
import com.eventflit.pushnotifications.logging.Logger
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class PushNotificationsAPI(private val appId: String) {
  private val baseUrl = "http://push.eventflit.com/device/app/$appId/"

  private val log = Logger.get(this::class)

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
      .create(PushNotificationService::class.java)

  private val jobQueue: ArrayList<(String) -> Unit> = ArrayList()

  var deviceId: String? = null
  var fcmToken: String? = null

  // Handles the JsonSyntaxException properly
  private fun safeExtractJsonError(possiblyJson: String): NOKResponse {
    return try {
      gson.fromJson(possiblyJson, NOKResponse::class.java)
    } catch (jsonException: JsonSyntaxException) {
      log.w("Failed to parse json `$possiblyJson`", jsonException)
      unknownNOKResponse
    }
  }

  fun registerOrRefreshFCM(token: String, operationCallback: OperationCallback) {
    deviceId?.let { dId ->
      if (fcmToken != null && fcmToken != token) {
        service.refreshToken(dId, RefreshToken(token))
          .enqueue(object : RequestCallbackWithExpBackoff<Void>() {
            override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
              if (response?.code() == 404) {
                deviceId = null
                registerOrRefreshFCM(token, operationCallback)
                return
              }
              response?.errorBody()?.let { responseErrorBody ->
                val error = safeExtractJsonError(responseErrorBody.string())
                log.w("Failed to register device: $error")
                operationCallback.onFailure(error)
                return
              }
              fcmToken = token
            }
          })
      }
      return
    }

    val call = service.register(
        RegisterRequest(
            token,
            DeviceMetadata(BuildConfig.VERSION_NAME, android.os.Build.VERSION.RELEASE)
        )
    )
    call.enqueue(object : RequestCallbackWithExpBackoff<RegisterResponse>() {
      override fun onResponse(call: Call<RegisterResponse>?, response: Response<RegisterResponse>?) {
        val responseBody = response?.body()
        if (responseBody != null) {
          deviceId = responseBody.id

          operationCallback.onSuccess()

          synchronized(jobQueue) {
            jobQueue.forEach {
              it(responseBody.id)
            }
            jobQueue.clear()
          }

          return
        }

        val responseErrorBody = response?.errorBody()
        if (responseErrorBody != null) {
          val error =
              try {
                gson.fromJson(responseErrorBody.string(), RegisterResponseError::class.java)
              } catch (jsonException: JsonSyntaxException) {
                log.w("Failed to parse json `${responseErrorBody.string()}`", jsonException)
                unknownNOKResponse
              }

          log.w("Failed to register device: $error")
          operationCallback.onFailure(error)
        }
      }
    })
  }

  fun subscribe(interest: String, operationCallback: OperationCallback) {
    val callback = object : RequestCallbackWithExpBackoff<Void>() {
      override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
        if (response != null && response.code() >= 200 && response.code() < 300) {
          log.d("Successfully subscribed to interest '$interest'")
          operationCallback.onSuccess()
          return
        }

        val responseErrorBody = response?.errorBody()
        if (responseErrorBody != null) {
          val error = safeExtractJsonError(responseErrorBody.string())
          log.w("Failed to subscribe to interest: $error")
          operationCallback.onFailure(error)
        }
      }
    }

    synchronized(jobQueue) {
      deviceId?.let {
        service.subscribe(deviceId = it, interest = interest)
          .enqueue(callback)
        return
      }
      jobQueue += {
        service.subscribe(deviceId = it, interest = interest)
          .enqueue(callback)
      }
    }
  }

  fun unsubscribe(interest: String, operationCallback: OperationCallback) {
    val callback = object : RequestCallbackWithExpBackoff<Void>() {
      override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
        if (response != null && response.code() >= 200 && response.code() < 300) {
          log.d("Successfully unsubscribed to interest '$interest'")
          operationCallback.onSuccess()
          return
        }

        val responseErrorBody = response?.errorBody()
        if (responseErrorBody != null) {
          val error = safeExtractJsonError(responseErrorBody.string())
          log.w("Failed to unsubscribe to interest: $error")
          operationCallback.onFailure(error)
        }
      }
    }

    synchronized(jobQueue) {
      deviceId?.let {
        service.unsubscribe(deviceId = it, interest = interest)
          .enqueue(callback)
        return
      }
      jobQueue += {
        service.unsubscribe(deviceId = it, interest = interest)
          .enqueue(callback)
      }
    }
  }

  fun setSubscriptions(interests: Set<String>, operationCallback: OperationCallback) {
    val callback = object : RequestCallbackWithExpBackoff<Void>() {
      override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
        if (response != null && response.code() >= 200 && response.code() < 300) {
          log.d("Successfully updated the interest set")
          operationCallback.onSuccess()
          return
        }

        val responseErrorBody = response?.errorBody()
        if (responseErrorBody != null) {
          val error = safeExtractJsonError(responseErrorBody.string())
          log.w("Failed to update the interest set: $error")
          operationCallback.onFailure(error)
        }
      }
    }

    synchronized(jobQueue) {
      deviceId?.let {
        service.setSubscriptions(
          deviceId = it, interests = SetSubscriptionsRequest(interests)
        ).enqueue(callback)
        return
      }
      jobQueue += {
        service.setSubscriptions(
          deviceId = it, interests = SetSubscriptionsRequest(interests)
        ).enqueue(callback)
      }
    }
  }

  fun setMetadata(metadata: DeviceMetadata, operationCallback: OperationCallback) {
    val callback = object : RequestCallbackWithExpBackoff<Void>() {
      override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
        if (response != null && response.code() >= 200 && response.code() < 300) {
          log.d("Successfully set metadata")
          operationCallback.onSuccess()
          return
        }

        val responseErrorBody = response?.errorBody()
        if (responseErrorBody != null) {
          val error = safeExtractJsonError(responseErrorBody.string())
          log.w("Failed to set metadata: $error")
          operationCallback.onFailure(error)
        }
      }
    }

    synchronized(jobQueue) {
      deviceId?.let {
        service.setMetadata(
          deviceId = it, metadata = metadata
        ).enqueue(callback)
        return
      }
      jobQueue += {
        service.setMetadata(
          deviceId = it, metadata = metadata
        ).enqueue(callback)
      }
    }
  }
}
