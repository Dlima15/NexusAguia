package br.com.fiap.gabinova.model

import com.google.gson.annotations.SerializedName

enum class UserRole {
    @SerializedName("ADMIN")         ADMIN,
    @SerializedName("MANAGER")       MANAGER,
    @SerializedName("ANALYST")       ANALYST,
    @SerializedName("COLLABORATOR")  COLLABORATOR
}

data class User(
    @SerializedName("id")           val id: String,
    @SerializedName("name")         val name: String,
    @SerializedName("email")        val email: String,
    @SerializedName("role")         val role: UserRole,
    @SerializedName("department")   val department: String,
    @SerializedName("avatar_url")   val avatarUrl: String? = null,
    @SerializedName("created_at")   val createdAt: String
)
