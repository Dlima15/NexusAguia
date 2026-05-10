package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.api.ApiService
import br.com.fiap.gabinova.data.remote.dto.CreateProjectRequest
import br.com.fiap.gabinova.data.remote.dto.ProjectDto
import br.com.fiap.gabinova.data.remote.dto.UpdateProjectProgressRequest
import br.com.fiap.gabinova.data.remote.dto.UpdateProjectRequest
import br.com.fiap.gabinova.data.remote.safeApiCall

class ProjectsRepository(private val apiService: ApiService) {

    suspend fun getProjects(): ApiResult<List<ProjectDto>> =
        safeApiCall { apiService.getProjects() }

    suspend fun createProject(
        title: String,
        ideaOrigin: String,
        responsible: String,
        status: String,
        stage: String,
        investment: String,
        expectedReturn: String,
        deadline: String
    ): ApiResult<ProjectDto> = safeApiCall {
        apiService.createProject(
            CreateProjectRequest(
                title          = title,
                ideaOrigin     = ideaOrigin,
                responsible    = responsible,
                status         = status,
                stage          = stage,
                investment     = investment,
                expectedReturn = expectedReturn,
                deadline       = deadline
            )
        )
    }

    suspend fun updateProject(
        id: String,
        title: String,
        ideaOrigin: String,
        responsible: String,
        status: String,
        stage: String,
        investment: String,
        expectedReturn: String,
        actualReturn: String,
        productivity: String,
        deadline: String
    ): ApiResult<ProjectDto> = safeApiCall {
        apiService.updateProject(
            id,
            UpdateProjectRequest(
                title          = title,
                ideaOrigin     = ideaOrigin,
                responsible    = responsible,
                status         = status,
                stage          = stage,
                investment     = investment,
                expectedReturn = expectedReturn,
                actualReturn   = actualReturn,
                productivity   = productivity,
                deadline       = deadline
            )
        )
    }

    suspend fun updateProgress(id: String, progress: Int): ApiResult<ProjectDto> =
        safeApiCall { apiService.updateProjectProgress(id, UpdateProjectProgressRequest(progress)) }
}
