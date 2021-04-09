package io.github.andreyfillipe.pix.excluir

import com.google.rpc.BadRequest
import io.github.andreyfillipe.ExcluirPixRequest
import io.github.andreyfillipe.KeyManagerExcluirGrpcServiceGrpc
import io.github.andreyfillipe.pix.*
import io.github.andreyfillipe.sistema_externo.banco_central.BancoCentralClient
import io.github.andreyfillipe.sistema_externo.banco_central.DeletarChavePixBcbRequest
import io.github.andreyfillipe.sistema_externo.banco_central.DeletarChavePixBcbResponse
import io.github.andreyfillipe.sistema_externo.itau.ItauClient
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ExcluirChavePixGrpcEndPointTest(
    private val pixRepository: PixRepository,
    private val grpcClient: KeyManagerExcluirGrpcServiceGrpc.KeyManagerExcluirGrpcServiceBlockingStub
) {

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @Inject
    lateinit var itauClient: ItauClient

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Inject
    lateinit var bancoCentralClient: BancoCentralClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        pixRepository.deleteAll()
    }

    @Test
    fun `Deve excluir chave pix`() {
        //cenario
        val pix = Pix(
            clienteId = CLIENTE_ID,
            tipoChave = TipoChave.CPF,
            valorChave = "12345678909",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "60701190",
                nomeTitular = "Nome do Titular",
                cpfTitular = "12345678909",
                agencia = "0001",
                numeroConta = "123456"
            )
        )
        pixRepository.save(pix)

        Mockito.`when`(bancoCentralClient.excluirChavePix(
            key = "12345678909",
            request = DeletarChavePixBcbRequest(
                key = "12345678909",
                participant = "60701190")))
            .thenReturn(HttpResponse.ok(DeletarChavePixBcbResponse(
                key = "12345678909",
                participant = "60701190",
                deletedAt = LocalDateTime.now())))

        //acao
        val response = grpcClient.excluir(ExcluirPixRequest.newBuilder()
            .setPixId(pix.id.toString())
            .setClienteId(CLIENTE_ID.toString())
            .build()
        )
        //validacao
        assertNotNull(response.pixId)
        assertNotNull(response.clienteId)
        assertEquals(pix.id.toString(), response.pixId)
        assertEquals(CLIENTE_ID.toString(), response.clienteId)
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Test
    fun `Não deve excluir chave pix quando pix não pertencer a clienteId`() {

    }

    @Factory
    class GrpcClientFactory {
        @Singleton
        fun excluirChavePix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerExcluirGrpcServiceGrpc.KeyManagerExcluirGrpcServiceBlockingStub {
            return KeyManagerExcluirGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun StatusRuntimeException.violations(): List<Pair<String, String>> {
        val details = StatusProto.fromThrowable(this)
            ?.detailsList?.get(0)!!
            .unpack(BadRequest::class.java)

        return details.fieldViolationsList
            .map { it.field to it. description }
    }
}