package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DashboardDto(
    @SerializedName("totalIdeas")           val totalIdeas: Int,
    @SerializedName("approvedIdeas")        val approvedIdeas: Int,
    @SerializedName("inReviewIdeas")        val inReviewIdeas: Int,
    @SerializedName("ideasThisMonth")       val ideasThisMonth: Int,
    @SerializedName("activeProjects")       val activeProjects: Int,
    @SerializedName("completedProjects")    val completedProjects: Int,
    @SerializedName("totalProjects")        val totalProjects: Int,
    @SerializedName("totalInvestment")      val totalInvestment: String,
    @SerializedName("financialReturn")      val financialReturn: String,
    @SerializedName("roi")                  val roi: Double,
    @SerializedName("economyGenerated")     val economyGenerated: String,
    @SerializedName("productivityGain")     val productivityGain: String,
    @SerializedName("engagedCollaborators") val engagedCollaborators: Int,
    @SerializedName("totalCollaborators")   val totalCollaborators: Int,
    @SerializedName("engagementRate")       val engagementRate: Double,
    @SerializedName("topGuideline")         val topGuideline: String
)
