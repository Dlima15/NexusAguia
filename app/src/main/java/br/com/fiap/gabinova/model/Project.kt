package br.com.fiap.gabinova.model

import com.google.gson.annotations.SerializedName

enum class ProjectStatus {
    @SerializedName("PLANNING")     PLANNING,
    @SerializedName("IN_PROGRESS")  IN_PROGRESS,
    @SerializedName("ON_HOLD")      ON_HOLD,
    @SerializedName("COMPLETED")    COMPLETED,
    @SerializedName("CANCELLED")    CANCELLED
}

data class Project(
    @SerializedName("id")           val id: String,
    @SerializedName("title")        val title: String,
    @SerializedName("description")  val description: String,
    @SerializedName("status")       val status: ProjectStatus,
    @SerializedName("progress")     val progress: Int,
    @SerializedName("team_ids")     val teamIds: List<String> = emptyList(),
    @SerializedName("idea_id")      val ideaId: String? = null,
    @SerializedName("start_date")   val startDate: String,
    @SerializedName("end_date")     val endDate: String? = null,
    @SerializedName("created_at")   val createdAt: String
)
