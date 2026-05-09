package br.com.fiap.gabinova.model

import com.google.gson.annotations.SerializedName

data class StrategicGuideline(
    @SerializedName("id")           val id: String,
    @SerializedName("title")        val title: String,
    @SerializedName("description")  val description: String,
    @SerializedName("category")     val category: String,
    @SerializedName("priority")     val priority: Int,
    @SerializedName("active")       val active: Boolean,
    @SerializedName("created_at")   val createdAt: String
)
