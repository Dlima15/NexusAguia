package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token")     val token: String,
    @SerializedName("userId")    val userId: String,
    @SerializedName("userName")  val userName: String,
    @SerializedName("userRole")  val userRole: String,
    @SerializedName("email")     val email: String,
    @SerializedName("expiresAt") val expiresAt: String
)
