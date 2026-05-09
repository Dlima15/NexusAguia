package br.com.fiap.gabinova.model

import com.google.gson.annotations.SerializedName

data class Badge(
    @SerializedName("id")           val id: String,
    @SerializedName("name")         val name: String,
    @SerializedName("description")  val description: String,
    @SerializedName("icon_url")     val iconUrl: String? = null,
    @SerializedName("earned")       val earned: Boolean = false,
    @SerializedName("earned_at")    val earnedAt: String? = null
)

data class GamificationProfile(
    @SerializedName("user_id")              val userId: String,
    @SerializedName("points")              val points: Int,
    @SerializedName("level")               val level: Int,
    @SerializedName("level_name")          val levelName: String,
    @SerializedName("ranking")             val ranking: Int,
    @SerializedName("badges")              val badges: List<Badge> = emptyList(),
    @SerializedName("current_level_points") val currentLevelPoints: Int,
    @SerializedName("next_level_points")    val nextLevelPoints: Int
)

data class RankingItem(
    @SerializedName("position")     val position: Int,
    @SerializedName("user_id")      val userId: String,
    @SerializedName("user_name")    val userName: String,
    @SerializedName("points")       val points: Int,
    @SerializedName("level")        val level: Int,
    @SerializedName("department")   val department: String
)
