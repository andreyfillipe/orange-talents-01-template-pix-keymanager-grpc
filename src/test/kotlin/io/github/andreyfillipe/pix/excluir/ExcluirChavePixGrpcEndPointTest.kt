package io.github.andreyfillipe.pix.excluir

import com.google.rpc.BadRequest
import io.github.andreyfillipe.ExcluirPixRequest
import io.github.andreyfillipe.KeyManagerExcluirGrpcServiceGrpc
import io.github.andreyfillipe.pix.*
import io.github.andreyfillipe.sistema_externo.banco_central.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun `Nao deve excluir chave pix quando pix nao pertencer a clienteId`() {
        //cenario
        val pixId = UUID.randomUUID().toString()
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

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.excluir(ExcluirPixRequest.newBuilder()
                .setPixId(pixId)
                .setClienteId(CLIENTE_ID.toString())
                .build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix: ${pixId} não encontrada ou não pertence ao Cliente ID: ${CLIENTE_ID.toString()}", error.status.description)
        assertTrue(pixRepository.findAll().count() == 1)
    }

    @Test
    fun `Nao deve excluir chave pix quando ocorrer erro no Banco Central`() {
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
                participant = "60701190")
        ))
            .thenReturn(HttpResponse.badRequest())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.excluir(ExcluirPixRequest.newBuilder()
                .setPixId(pix.id.toString())
                .setClienteId(CLIENTE_ID.toString())
                .build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Erro ao deletar chave Pix no Banco Central do Brasil", error.status.description)
        assertTrue(pixRepository.findAll().count() == 1)
    }

    @Test
    fun `Nao deve excluir chave pix quando parametros de entrada forem invalidos`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.excluir(ExcluirPixRequest.newBuilder().build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("request with invalid parameters", error.status.description)
        MatcherAssert.assertThat(error.violations(), Matchers.containsInAnyOrder(
            //Pair("pixId", "must not be null"),
            //Pair("request", "Chave Pix inválida"),
            Pair("clienteId", "must not be blank")
        ))
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