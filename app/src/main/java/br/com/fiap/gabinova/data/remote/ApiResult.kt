package br.com.fiap.gabinova.data.remote

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) ApiResult.Success(body)
            else ApiResult.Error("Resposta vazia do servidor.", response.code())
        } else {
            ApiResult.Error(
                message = response.errorBody()?.string() ?: "Erro ${response.code()}",
                code    = response.code()
            )
        }
    } catch (e: java.net.UnknownHostException) {
        ApiResult.Error("Sem conexão com a internet.")
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error("Tempo de conexão esgotado.")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Erro inesperado.")
    }
}
