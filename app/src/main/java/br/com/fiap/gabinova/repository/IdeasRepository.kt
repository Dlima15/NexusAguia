package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.api.ApiService
import br.com.fiap.gabinova.data.remote.dto.CreateIdeaRequest
import br.com.fiap.gabinova.data.remote.dto.IdeaDto
import br.com.fiap.gabinova.data.remote.dto.UpdateIdeaPriorityRequest
import br.com.fiap.gabinova.data.remote.dto.UpdateIdeaStatusRequest
import br.com.fiap.gabinova.data.remote.safeApiCall

class IdeasRepository(private val apiService: ApiService) {

    suspend fun getIdeas(): ApiResult<List<IdeaDto>> =
        safeApiCall { apiService.getIdeas() }

    suspend fun getIdeasByUser(userId: String): ApiResult<List<IdeaDto>> =
        safeApiCall { apiService.getIdeasByUser(userId) }

    suspend fun createIdea(
        title: String,
        description: String,
        sector: String,
        category: String,
        expectedImpact: String,
        urgency: Int
    ): ApiResult<IdeaDto> = safeApiCall {
        apiService.createIdea(
            CreateIdeaRequest(
                title          = title,
                description    = description,
                sector         = sector,
                category       = category,
                expectedImpact = expectedImpact,
                urgency        = urgency
            )
        )
    }

    suspend fun updateIdea(
        id: String,
        title: String,
        description: String,
        sector: String,
        category: String,
        expectedImpact: String,
        urgency: Int
    ): ApiResult<IdeaDto> = safeApiCall {
        apiService.updateIdea(
            id,
            CreateIdeaRequest(
                title          = title,
                description    = description,
                sector         = sector,
                category       = category,
                expectedImpact = expectedImpact,
                urgency        = urgency
            )
        )
    }

    suspend fun updateStatus(id: String, status: String): ApiResult<IdeaDto> =
        safeApiCall { apiService.updateIdeaStatus(id, UpdateIdeaStatusRequest(status)) }

    suspend fun updatePriority(id: String, priority: String): ApiResult<IdeaDto> =
        safeApiCall { apiService.updateIdeaPriority(id, UpdateIdeaPriorityRequest(priority)) }
}
