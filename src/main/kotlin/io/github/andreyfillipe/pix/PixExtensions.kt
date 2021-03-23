package io.github.andreyfillipe.pix

import io.github.andreyfillipe.CadastrarPixRequest
import io.github.andreyfillipe.ConsultarPixRequest
import io.github.andreyfillipe.ConsultarPixRequest.FiltroCase.*
import io.github.andreyfillipe.ExcluirPixRequest
import io.github.andreyfillipe.TipoChave.*
import io.github.andreyfillipe.TipoConta.*
import io.github.andreyfillipe.pix.cadastrar.NovaChavePixRequest
import io.github.andreyfillipe.pix.consultar.Filtro
import io.github.andreyfillipe.pix.excluir.ExcluirChavePixRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun CadastrarPixRequest.toNovaChavePixRequest(): NovaChavePixRequest {
    return NovaChavePixRequest(
        clienteId = clienteId,
        tipoChave = when (tipoChave) {
            CHAVE_DESCONHECIDA -> null
            else -> TipoChave.valueOf(tipoChave.name)
        },
        valorChave = valorChave,
        tipoConta = when (tipoConta) {
            CONTA_DESCONHECIDA -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }
    )
}

fun ExcluirPixRequest.toExcluirChavePixRequest(): ExcluirChavePixRequest {
    return ExcluirChavePixRequest(
        clienteId = clienteId,
        pixId = pixId
    )
}

fun ConsultarPixRequest.toFiltro(validator: Validator): Filtro {
    val filtro = when(filtroCase) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        VALORCHAVE -> Filtro.PorChave(valorChave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }
    return filtro
}