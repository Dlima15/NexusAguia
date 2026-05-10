package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.api.ApiService
import br.com.fiap.gabinova.data.remote.dto.UserDto
import br.com.fiap.gabinova.data.remote.safeApiCall

class UserRepository(private val apiService: ApiService) {

    suspend fun getUsers(): ApiResult<List<UserDto>> =
        safeApiCall { apiService.getUsers() }

    suspend fun getUserById(id: String): ApiResult<UserDto> =
        safeApiCall { apiService.getUserById(id) }
}
