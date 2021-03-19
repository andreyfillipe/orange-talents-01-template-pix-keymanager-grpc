package io.github.andreyfillipe.validacao.handlers

import io.github.andreyfillipe.validacao.ExceptionHandler
import javax.inject.Singleton

@Singleton
class ValidacaoException : ExceptionHandler<ApiErroException> {
    override fun handle(e: ApiErroException): ExceptionHandler.StatusWrapper {
        return ExceptionHandler.StatusWrapper(
            e.httpStatus
            .withDescription(e.mensagem)
            .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ApiErroException
    }
}