package io.github.andreyfillipe.pix.consultar

import io.github.andreyfillipe.pix.PixRepository
import io.github.andreyfillipe.sistema_externo.banco_central.BancoCentralClient
import io.github.andreyfillipe.validacao.beanValidation.ValidarUUID
import io.github.andreyfillipe.validacao.handlers.ApiErroException
import io.grpc.Status
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(pixRepository: PixRepository, bancoCentralClient: BancoCentralClient): ConsultarChavePixResponse

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidarUUID val clienteId: String,
        @field:NotBlank @field:ValidarUUID val pixId: String
    ) : Filtro() {
        fun pixIdAsUuid() = UUID.fromString(pixId)
        fun clienteIdAsUuid() = UUID.fromString(clienteId)

        override fun filtra(pixRepository: PixRepository, bancoCentralClient: BancoCentralClient): ConsultarChavePixResponse {
            return pixRepository.findByIdAndClienteId(pixIdAsUuid(), clienteIdAsUuid())
                .map(ConsultarChavePixResponse::of)
                .orElseThrow { ApiErroException(Status.NOT_FOUND, "Chave Pix não encontrada") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val valorChave: String) : Filtro() {
        override fun filtra(pixRepository: PixRepository, bancoCentralClient: BancoCentralClient): ConsultarChavePixResponse {
            return pixRepository.findByValorChave(valorChave)
                .map(ConsultarChavePixResponse::of)
                .orElseGet {
                    val response = bancoCentralClient.consultarChavePix(valorChave)
                    when(response.status) {
                        HttpStatus.OK -> response.body()?.toConsultarChavePixResponse()
                        else -> throw ApiErroException(Status.NOT_FOUND, "Chave Pix não encontrada")
                    }
                }
        }
    }

    @Introspected
    class Invalido() : Filtro() {
        override fun filtra(pixRepository: PixRepository, bancoCentralClient: BancoCentralClient): ConsultarChavePixResponse {
            throw ApiErroException(Status.NOT_FOUND, "Chave Pix inválida ou não informada não encontrada")
        }
    }
}