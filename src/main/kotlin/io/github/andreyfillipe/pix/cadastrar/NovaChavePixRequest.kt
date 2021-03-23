package io.github.andreyfillipe.pix.cadastrar

import io.github.andreyfillipe.pix.Conta
import io.github.andreyfillipe.pix.Pix
import io.github.andreyfillipe.pix.TipoChave
import io.github.andreyfillipe.pix.TipoConta
import io.github.andreyfillipe.validacao.beanValidation.ValidarChavePix
import io.github.andreyfillipe.validacao.beanValidation.ValidarUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidarChavePix
@Introspected
data class NovaChavePixRequest(
    @ValidarUUID
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:NotBlank
    @field:Size(max = 77)
    val valorChave: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {

    fun toPix(conta: Conta): Pix =
        Pix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoChave.valueOf(this.tipoChave!!.name),
            valorChave = if (this.tipoChave == TipoChave.CHAVE_ALEATORIA) UUID.randomUUID().toString() else this.valorChave!!,
            tipoConta = TipoConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
}