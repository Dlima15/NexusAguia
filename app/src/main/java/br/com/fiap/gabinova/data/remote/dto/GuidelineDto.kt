package br.com.fiap.gabinova.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GuidelineDto(
    @SerializedName("id")          val id: String,
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category")    val category: String,
    @SerializedName("priority")    val priority: String,
    @SerializedName("status")      val status: String,
    @SerializedName("createdAt")   val createdAt: String,
    @SerializedName("updatedAt")   val updatedAt: String
)

data class CreateGuidelineRequest(
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category")    val category: String,
    @SerializedName("priority")    val priority: String
)
