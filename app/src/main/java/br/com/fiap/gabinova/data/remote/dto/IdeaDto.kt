package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class IdeaDto(
    @SerializedName("id")             val id: String,
    @SerializedName("title")          val title: String,
    @SerializedName("description")    val description: String,
    @SerializedName("sector")         val sector: String,
    @SerializedName("category")       val category: String,
    @SerializedName("expectedImpact") val expectedImpact: String,
    @SerializedName("urgency")        val urgency: Int,
    @SerializedName("status")         val status: String,
    @SerializedName("authorId")       val authorId: String,
    @SerializedName("authorName")     val authorName: String,
    @SerializedName("score")          val score: Int,
    @SerializedName("createdAt")      val createdAt: String,
    @SerializedName("updatedAt")      val updatedAt: String
)

data class CreateIdeaRequest(
    @SerializedName("title")          val title: String,
    @SerializedName("description")    val description: String,
    @SerializedName("sector")         val sector: String,
    @SerializedName("category")       val category: String,
    @SerializedName("expectedImpact") val expectedImpact: String,
    @SerializedName("urgency")        val urgency: Int
)

data class UpdateIdeaStatusRequest(
    @SerializedName("status") val status: String
)

data class UpdateIdeaPriorityRequest(
    @SerializedName("priority") val priority: String
)
