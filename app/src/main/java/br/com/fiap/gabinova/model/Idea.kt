package br.com.fiap.gabinova.model

import com.google.gson.annotations.SerializedName

enum class IdeaStatus {
    @SerializedName("PENDING")      PENDING,
    @SerializedName("IN_REVIEW")    IN_REVIEW,
    @SerializedName("APPROVED")     APPROVED,
    @SerializedName("REJECTED")     REJECTED,
    @SerializedName("IMPLEMENTED")  IMPLEMENTED
}

data class Idea(
    @SerializedName("id")             val id: String,
    @SerializedName("title")          val title: String,
    @SerializedName("description")    val description: String,
    @SerializedName("status")         val status: IdeaStatus,
    @SerializedName("author_id")      val authorId: String,
    @SerializedName("author_name")    val authorName: String,
    @SerializedName("guideline_id")   val guidelineId: String? = null,
    @SerializedName("tags")           val tags: List<String> = emptyList(),
    @SerializedName("upvotes")        val upvotes: Int = 0,
    @SerializedName("created_at")     val createdAt: String,
    @SerializedName("updated_at")     val updatedAt: String
)
