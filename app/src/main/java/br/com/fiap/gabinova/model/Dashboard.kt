package br.com.fiap.gabinova.model

import com.google.gson.annotations.SerializedName

data class DashboardResult(
    @SerializedName("total_ideas")          val totalIdeas: Int,
    @SerializedName("approved_ideas")       val approvedIdeas: Int,
    @SerializedName("active_projects")      val activeProjects: Int,
    @SerializedName("completed_projects")   val completedProjects: Int,
    @SerializedName("total_collaborators")  val totalCollaborators: Int,
    @SerializedName("ideas_this_month")     val ideasThisMonth: Int,
    @SerializedName("top_guideline")        val topGuideline: String? = null,
    @SerializedName("engagement_rate")      val engagementRate: Double
)
