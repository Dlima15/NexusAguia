package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")         val id: String,
    @SerializedName("name")       val name: String,
    @SerializedName("email")      val email: String,
    @SerializedName("role")       val role: String,
    @SerializedName("department") val department: String,
    @SerializedName("points")     val points: Int,
    @SerializedName("level")      val level: Int,
    @SerializedName("createdAt")  val createdAt: String
)
