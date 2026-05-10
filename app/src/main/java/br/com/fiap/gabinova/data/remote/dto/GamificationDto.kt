package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GamificationDto(
    @SerializedName("userId")    val userId: String,
    @SerializedName("userName")  val userName: String,
    @SerializedName("points")    val points: Int,
    @SerializedName("level")     val level: Int,
    @SerializedName("levelName") val levelName: String,
    @SerializedName("progress")  val progress: Float,
    @SerializedName("badges")    val badges: List<BadgeDto>,
    @SerializedName("history")   val history: List<ScoreHistoryDto>
)

data class BadgeDto(
    @SerializedName("id")       val id: String,
    @SerializedName("name")     val name: String,
    @SerializedName("earned")   val earned: Boolean,
    @SerializedName("earnedAt") val earnedAt: String?
)

data class ScoreHistoryDto(
    @SerializedName("points")      val points: Int,
    @SerializedName("description") val description: String,
    @SerializedName("date")        val date: String,
    @SerializedName("eventType")   val eventType: String
)

data class AddPointsRequest(
    @SerializedName("userId")      val userId: String,
    @SerializedName("points")      val points: Int,
    @SerializedName("eventType")   val eventType: String,
    @SerializedName("description") val description: String
)
