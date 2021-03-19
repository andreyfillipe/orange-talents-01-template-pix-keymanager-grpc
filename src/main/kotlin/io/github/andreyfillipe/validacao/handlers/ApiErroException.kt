package io.github.andreyfillipe.validacao.handlers

import io.grpc.Status
import java.lang.RuntimeException

class ApiErroException(
    val httpStatus: Status,
    val mensagem: String
) : RuntimeException(mensagem) {
}