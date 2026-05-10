package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.api.ApiService
import br.com.fiap.gabinova.data.remote.dto.AddPointsRequest
import br.com.fiap.gabinova.data.remote.dto.GamificationDto
import br.com.fiap.gabinova.data.remote.dto.RankingDto
import br.com.fiap.gabinova.data.remote.safeApiCall

class GamificationRepository(private val apiService: ApiService) {

    suspend fun getGamification(userId: String): ApiResult<GamificationDto> =
        safeApiCall { apiService.getGamification(userId) }

    suspend fun getRanking(): ApiResult<List<RankingDto>> =
        safeApiCall { apiService.getRanking() }

    suspend fun addPoints(
        userId: String,
        points: Int,
        eventType: String,
        description: String
    ): ApiResult<GamificationDto> = safeApiCall {
        apiService.addPoints(
            AddPointsRequest(
                userId      = userId,
                points      = points,
                eventType   = eventType,
                description = description
            )
        )
    }
}
