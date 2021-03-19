package io.github.andreyfillipe.pix.cadastrar

import io.micronaut.core.annotation.Introspected

@Introspected
enum class TipoContaRequest {
    CONTA_CORRENTE,
    CONTA_POUPANCA
}
