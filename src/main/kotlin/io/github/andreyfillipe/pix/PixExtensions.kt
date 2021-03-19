package io.github.andreyfillipe.pix

import io.github.andreyfillipe.CadastrarPixRequest
import io.github.andreyfillipe.ExcluirPixRequest
import io.github.andreyfillipe.TipoChave.*
import io.github.andreyfillipe.TipoConta.*
import io.github.andreyfillipe.pix.cadastrar.NovaChavePixRequest
import io.github.andreyfillipe.pix.cadastrar.TipoChaveRequest
import io.github.andreyfillipe.pix.cadastrar.TipoContaRequest
import io.github.andreyfillipe.pix.excluir.ExcluirChavePixRequest

fun CadastrarPixRequest.toNovaChavePixRequest(): NovaChavePixRequest {
    return NovaChavePixRequest(
        clienteId = clienteId,
        tipoChave = when (tipoChave) {
            CHAVE_DESCONHECIDA -> null
            else -> TipoChaveRequest.valueOf(tipoChave.name)
        },
        valorChave = valorChave,
        tipoConta = when (tipoConta) {
            CONTA_DESCONHECIDA -> null
            else -> TipoContaRequest.valueOf(tipoConta.name)
        }
    )
}

fun ExcluirPixRequest.toExcluirChavePixRequest(): ExcluirChavePixRequest {
    return ExcluirChavePixRequest(
        clienteId = clienteId,
        pixId = pixId
    )
}