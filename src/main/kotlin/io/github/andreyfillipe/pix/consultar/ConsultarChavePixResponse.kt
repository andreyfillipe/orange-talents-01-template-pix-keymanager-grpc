package io.github.andreyfillipe.pix.consultar

import io.github.andreyfillipe.pix.Conta
import io.github.andreyfillipe.pix.Pix
import io.github.andreyfillipe.pix.TipoChave
import io.github.andreyfillipe.pix.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ConsultarChavePixResponse(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoChave,
    val valorChave: String,
    val tipoConta: TipoConta,
    val conta: Conta,
    val criadoEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(pix: Pix): ConsultarChavePixResponse {
            return ConsultarChavePixResponse(
                pixId = pix.id,
                clienteId = pix.clienteId,
                tipo = pix.tipoChave,
                valorChave = pix.valorChave,
                tipoConta = pix.tipoConta,
                conta = pix.conta,
                criadoEm = pix.criadoEm,
            )
        }
    }
}