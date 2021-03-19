package io.github.andreyfillipe.validacao

import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(
    private val handlers: List<ExceptionHandler<Exception>>
) {

    fun resolve(e: Exception): ExceptionHandler<Exception> {
        val filteredHandlers = handlers.filter { it.supports(e) }
        return filteredHandlers.first()
    }

}