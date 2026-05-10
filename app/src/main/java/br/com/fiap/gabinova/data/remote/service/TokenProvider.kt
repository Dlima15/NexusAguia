package br.com.fiap.gabinova.data.remote.service

object TokenProvider {
    @Volatile
    var token: String = ""
}
