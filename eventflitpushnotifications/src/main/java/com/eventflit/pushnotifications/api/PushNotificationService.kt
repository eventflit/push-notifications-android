package com.eventflit.pushnotifications.api;

import retrofit2.Call
import retrofit2.http.*

interface PushNotificationService {
  @POST("fcm")
  fun register(
    @Body registerRequest: RegisterRequest
  ): Call<RegisterResponse>

  @PUT("fcm/{deviceId}/token")
  fun refreshToken(
    @Path("deviceId") deviceId: String,
    @Body refreshToken: RefreshToken
  ): Call<Void>

  @POST("fcm/{deviceId}/interests/{interest}")
  fun subscribe(
    @Path("deviceId") deviceId: String,
    @Path("interest") interest: String
  ): Call<Void>

  @DELETE("fcm/{deviceId}/interests/{interest}")
  fun unsubscribe(
    @Path("deviceId") deviceId: String,
    @Path("interest") interest: String
  ): Call<Void>

  @PUT("fcm/{deviceId}/interests/")
  fun setSubscriptions(
    @Path("deviceId") deviceId: String,
    @Body interests: SetSubscriptionsRequest
  ): Call<Void>

  @PUT("fcm/{deviceId}/metadata")
  fun setMetadata(
    @Path("deviceId") deviceId: String,
    @Body metadata: DeviceMetadata
  ): Call<Void>
}

data class NOKResponse(
  val error: String,
  val desc: String
): RuntimeException() {
  override val message: String?
    get() = this.toString()
}

val unknownNOKResponse = NOKResponse("Unknown Service Error", "Something went wrong")

data class RefreshToken(
  val token: String
)

data class RegisterRequest(
  val token: String,
  val metadata: DeviceMetadata
)

data class DeviceMetadata (
  val sdkVersion: String,
  val androidVersion: String
)

data class RegisterResponse(
  val id: String
)

data class RegisterResponseError(
  val error: String,
  val desc: String,
  val tokenValidationResponse: TokenValidationResponse
): RuntimeException() {
  override val message: String?
    get() = this.toString()
}

data class TokenValidationResponse(
  val error: String,
  val details: String,
  val clientId: String,
  val messageId: String,
  val sentDeviceToken: String,
  val success: Boolean,
  val platform: String,
  val receivedDeviceToken: String
)

data class SetSubscriptionsRequest(
  val interests: Set<String>
)
