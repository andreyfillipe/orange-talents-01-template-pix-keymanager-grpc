package io.github.andreyfillipe.pix.cadastrar

import io.github.andreyfillipe.pix.Pix
import io.github.andreyfillipe.pix.PixRepository
import io.github.andreyfillipe.sistema_externo.banco_central.BancoCentralClient
import io.github.andreyfillipe.sistema_externo.itau.ItauClient
import io.github.andreyfillipe.validacao.handlers.ApiErroException
import io.grpc.Status
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class CadastrarChavePixService(
    private val pixRepository: PixRepository,
    private val itauClient: ItauClient,
    private val bancoCentralClient: BancoCentralClient
) {

    @Transactional
    fun cadastrar(@Valid request: NovaChavePixRequest): Pix {
        if (this.pixRepository.existsByValorChave(request.valorChave!!)) {
            throw ApiErroException(Status.ALREADY_EXISTS, "Chave Pix: ${request.valorChave} já existe")
        }

        val itauResponse = itauClient.buscarContaPorChaveETipo(request.clienteId!!, request.tipoConta!!.name)
        val conta = itauResponse.body()?.toConta() ?: throw ApiErroException(Status.NOT_FOUND, "Cliente não encontrado no Itaú")

        val pix = request.toPix(conta)
        this.pixRepository.save(pix)

        val bcbRequest = pix.toCadastrarChavePixBcbRequest()
        val bcbResponse = bancoCentralClient.cadastrarChavePix(bcbRequest)
        if (bcbResponse.status != HttpStatus.CREATED) {
            throw ApiErroException(Status.INTERNAL ,"Erro ao cadastrar chave Pix no Banco Central do Brasil")
        }
        pix.valorChave = bcbResponse.body().key

        return pix
    }
}