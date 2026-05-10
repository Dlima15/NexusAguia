package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.api.ApiService
import br.com.fiap.gabinova.data.remote.dto.CreateGuidelineRequest
import br.com.fiap.gabinova.data.remote.dto.GuidelineDto
import br.com.fiap.gabinova.data.remote.safeApiCall

class GuidelineRepository(private val apiService: ApiService) {

    suspend fun getGuidelines(): ApiResult<List<GuidelineDto>> =
        safeApiCall { apiService.getGuidelines() }

    suspend fun createGuideline(
        title: String,
        description: String,
        category: String,
        priority: String
    ): ApiResult<GuidelineDto> = safeApiCall {
        apiService.createGuideline(
            CreateGuidelineRequest(
                title       = title,
                description = description,
                category    = category,
                priority    = priority
            )
        )
    }

    suspend fun updateGuideline(
        id: String,
        title: String,
        description: String,
        category: String,
        priority: String
    ): ApiResult<GuidelineDto> = safeApiCall {
        apiService.updateGuideline(
            id,
            CreateGuidelineRequest(
                title       = title,
                description = description,
                category    = category,
                priority    = priority
            )
        )
    }

    suspend fun deleteGuideline(id: String): ApiResult<Unit> =
        safeApiCall { apiService.deleteGuideline(id) }
}
