package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.api.ApiService
import br.com.fiap.gabinova.data.remote.dto.DashboardDto
import br.com.fiap.gabinova.data.remote.safeApiCall

class DashboardRepository(private val apiService: ApiService) {

    suspend fun getDashboard(): ApiResult<DashboardDto> =
        safeApiCall { apiService.getDashboard() }
}
