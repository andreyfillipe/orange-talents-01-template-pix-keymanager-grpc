package io.github.andreyfillipe.pix.excluir

import io.github.andreyfillipe.validacao.beanValidation.ValidarUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ExcluirChavePixRequest(
    @field:NotNull
    val pixId: String?,
    @ValidarUUID
    @field:NotBlank
    val clienteId: String?
) {
}