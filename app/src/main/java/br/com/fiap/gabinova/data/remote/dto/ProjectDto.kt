package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProjectDto(
    @SerializedName("id")             val id: String,
    @SerializedName("title")          val title: String,
    @SerializedName("ideaOrigin")     val ideaOrigin: String,
    @SerializedName("responsible")    val responsible: String,
    @SerializedName("status")         val status: String,
    @SerializedName("stage")          val stage: String,
    @SerializedName("investment")     val investment: String,
    @SerializedName("expectedReturn") val expectedReturn: String,
    @SerializedName("actualReturn")   val actualReturn: String,
    @SerializedName("productivity")   val productivity: String,
    @SerializedName("progress")       val progress: Int,
    @SerializedName("deadline")       val deadline: String,
    @SerializedName("createdAt")      val createdAt: String,
    @SerializedName("updatedAt")      val updatedAt: String
)

data class CreateProjectRequest(
    @SerializedName("title")          val title: String,
    @SerializedName("ideaOrigin")     val ideaOrigin: String,
    @SerializedName("responsible")    val responsible: String,
    @SerializedName("status")         val status: String,
    @SerializedName("stage")          val stage: String,
    @SerializedName("investment")     val investment: String,
    @SerializedName("expectedReturn") val expectedReturn: String,
    @SerializedName("deadline")       val deadline: String
)

data class UpdateProjectRequest(
    @SerializedName("title")          val title: String,
    @SerializedName("ideaOrigin")     val ideaOrigin: String,
    @SerializedName("responsible")    val responsible: String,
    @SerializedName("status")         val status: String,
    @SerializedName("stage")          val stage: String,
    @SerializedName("investment")     val investment: String,
    @SerializedName("expectedReturn") val expectedReturn: String,
    @SerializedName("actualReturn")   val actualReturn: String,
    @SerializedName("productivity")   val productivity: String,
    @SerializedName("deadline")       val deadline: String
)

data class UpdateProjectProgressRequest(
    @SerializedName("progress") val progress: Int
)
