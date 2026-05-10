package br.com.fiap.gabinova.data.remote.api

import br.com.fiap.gabinova.data.remote.dto.AddPointsRequest
import br.com.fiap.gabinova.data.remote.dto.CreateGuidelineRequest
import br.com.fiap.gabinova.data.remote.dto.CreateIdeaRequest
import br.com.fiap.gabinova.data.remote.dto.CreateProjectRequest
import br.com.fiap.gabinova.data.remote.dto.DashboardDto
import br.com.fiap.gabinova.data.remote.dto.GamificationDto
import br.com.fiap.gabinova.data.remote.dto.GuidelineDto
import br.com.fiap.gabinova.data.remote.dto.IdeaDto
import br.com.fiap.gabinova.data.remote.dto.LoginRequest
import br.com.fiap.gabinova.data.remote.dto.LoginResponse
import br.com.fiap.gabinova.data.remote.dto.ProjectDto
import br.com.fiap.gabinova.data.remote.dto.RankingDto
import br.com.fiap.gabinova.data.remote.dto.UpdateIdeaPriorityRequest
import br.com.fiap.gabinova.data.remote.dto.UpdateIdeaStatusRequest
import br.com.fiap.gabinova.data.remote.dto.UpdateProjectProgressRequest
import br.com.fiap.gabinova.data.remote.dto.UpdateProjectRequest
import br.com.fiap.gabinova.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ── Users ─────────────────────────────────────────────────────────────────
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserDto>

    // ── Guidelines ────────────────────────────────────────────────────────────
    @GET("guidelines")
    suspend fun getGuidelines(): Response<List<GuidelineDto>>

    @POST("guidelines")
    suspend fun createGuideline(@Body request: CreateGuidelineRequest): Response<GuidelineDto>

    @PUT("guidelines/{id}")
    suspend fun updateGuideline(
        @Path("id") id: String,
        @Body request: CreateGuidelineRequest
    ): Response<GuidelineDto>

    @DELETE("guidelines/{id}")
    suspend fun deleteGuideline(@Path("id") id: String): Response<Unit>

    // ── Ideas ─────────────────────────────────────────────────────────────────
    @GET("ideas")
    suspend fun getIdeas(): Response<List<IdeaDto>>

    @GET("ideas/user/{userId}")
    suspend fun getIdeasByUser(@Path("userId") userId: String): Response<List<IdeaDto>>

    @POST("ideas")
    suspend fun createIdea(@Body request: CreateIdeaRequest): Response<IdeaDto>

    @PUT("ideas/{id}")
    suspend fun updateIdea(
        @Path("id") id: String,
        @Body request: CreateIdeaRequest
    ): Response<IdeaDto>

    @PATCH("ideas/{id}/status")
    suspend fun updateIdeaStatus(
        @Path("id") id: String,
        @Body request: UpdateIdeaStatusRequest
    ): Response<IdeaDto>

    @PATCH("ideas/{id}/priority")
    suspend fun updateIdeaPriority(
        @Path("id") id: String,
        @Body request: UpdateIdeaPriorityRequest
    ): Response<IdeaDto>

    // ── Projects ──────────────────────────────────────────────────────────────
    @GET("projects")
    suspend fun getProjects(): Response<List<ProjectDto>>

    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ProjectDto>

    @PUT("projects/{id}")
    suspend fun updateProject(
        @Path("id") id: String,
        @Body request: UpdateProjectRequest
    ): Response<ProjectDto>

    @PATCH("projects/{id}/progress")
    suspend fun updateProjectProgress(
        @Path("id") id: String,
        @Body request: UpdateProjectProgressRequest
    ): Response<ProjectDto>

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardDto>

    // ── Gamification ──────────────────────────────────────────────────────────
    @GET("gamification/{userId}")
    suspend fun getGamification(@Path("userId") userId: String): Response<GamificationDto>

    @GET("ranking")
    suspend fun getRanking(): Response<List<RankingDto>>

    @POST("gamification/points")
    suspend fun addPoints(@Body request: AddPointsRequest): Response<GamificationDto>
}
