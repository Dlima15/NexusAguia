package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RankingDto(
    @SerializedName("position")   val position: Int,
    @SerializedName("userId")     val userId: String,
    @SerializedName("userName")   val userName: String,
    @SerializedName("points")     val points: Int,
    @SerializedName("department") val department: String
)
