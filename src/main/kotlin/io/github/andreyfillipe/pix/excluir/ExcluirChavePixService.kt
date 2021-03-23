package io.github.andreyfillipe.pix.excluir

import io.github.andreyfillipe.pix.PixRepository
import io.github.andreyfillipe.sistema_externo.banco_central.BancoCentralClient
import io.github.andreyfillipe.sistema_externo.banco_central.DeletarChavePixBcbRequest
import io.github.andreyfillipe.validacao.handlers.ApiErroException
import io.grpc.Status
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class ExcluirChavePixService(
    private val pixRepository: PixRepository,
    private val bancoCentralClient: BancoCentralClient
) {

    @Transactional
    fun excluir(@Valid request: ExcluirChavePixRequest) {
        val pix = this.pixRepository
            .findByIdAndClienteId(UUID.fromString(request.pixId), UUID.fromString(request.clienteId))
            .orElseThrow { ApiErroException(Status.NOT_FOUND, "Chave Pix: ${request.pixId} não encontrada ou não pertence ao Cliente ID: ${request.clienteId}") }

        pixRepository.delete(pix)

        val deletarChavePixBcbRequest = DeletarChavePixBcbRequest(pix.valorChave, pix.conta.isbp())
        val bancoCentralResponse = bancoCentralClient.excluirChavePix(key = pix.valorChave, request = deletarChavePixBcbRequest)
        if (bancoCentralResponse.status != HttpStatus.OK) {
            throw ApiErroException(Status.INTERNAL, "Erro ao deletar chave Pix no Banco Central do Brasil")
        }
    }
}