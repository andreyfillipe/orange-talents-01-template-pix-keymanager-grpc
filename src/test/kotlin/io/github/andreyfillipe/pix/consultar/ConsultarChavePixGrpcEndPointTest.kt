package io.github.andreyfillipe.pix.consultar

import io.github.andreyfillipe.ConsultarPixRequest
import io.github.andreyfillipe.KeyManagerConsultarGrpcServiceGrpc
import io.github.andreyfillipe.pix.*
import io.github.andreyfillipe.sistema_externo.banco_central.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
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
internal class ConsultarChavePixGrpcEndPointTest(
    val pixRepository: PixRepository,
    val grpcClient: KeyManagerConsultarGrpcServiceGrpc.KeyManagerConsultarGrpcServiceBlockingStub
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
    fun `Deve consultar chave pix por pixId no banco local`() {
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

        //acao
        val response = grpcClient.consultar(ConsultarPixRequest.newBuilder()
            .setPixId(ConsultarPixRequest.FiltroPorPixId.newBuilder()
                .setPixId(pix.id.toString())
                .setClienteId(CLIENTE_ID.toString())
                .build()
                )
            .build()
        )

        //validacao
        assertEquals(pix.id.toString(), response.pixId)
        assertEquals(CLIENTE_ID.toString(), response.clienteId)
        assertEquals(KeyType.CPF, KeyType.valueOf(response.chave.tipoChave.name))
        assertEquals("12345678909", response.chave.valorChave)
        assertTrue(pixRepository.findAll().count() == 1)
    }

    @Test
    fun `Nao deve consultar chave pix por pixId no banco local`() {
        //cenario

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(ConsultarPixRequest.newBuilder()
                .setPixId(ConsultarPixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setClienteId(CLIENTE_ID.toString())
                    .build()
                )
                .build()
            )
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix não encontrada", error.status.description)
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Test
    fun `Deve consultar chave pix por valorChave no banco local`() {
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

        //acao
        val response = grpcClient.consultar(ConsultarPixRequest.newBuilder()
            .setValorChave("12345678909")
            .build()
        )

        //validacao
        assertEquals(pix.id.toString(), response.pixId)
        assertEquals(CLIENTE_ID.toString(), response.clienteId)
        assertEquals(KeyType.CPF, KeyType.valueOf(response.chave.tipoChave.name))
        assertEquals("12345678909", response.chave.valorChave)
        assertTrue(pixRepository.findAll().count() == 1)
    }

    @Test
    fun `Deve consultar chave pix por valorChave no banco central do brasil`() {
        //cenario
        Mockito.`when`(bancoCentralClient.consultarChavePix(
            key = "12345678909"
        ))
            .thenReturn(
                HttpResponse.ok(ConsultarChavePixBcbResponse(
                    keyType = KeyType.CPF,
                    key = "12345678909",
                    bankAccount = BankAccount(
                        participant = "60701190",
                        branch = "0001",
                        accountNumber = "123456",
                        accountType = AccountType.CACC),
                    owner = Owner(
                        type = Type.NATURAL_PERSON,
                        name = "Nome do Titular",
                        taxIdNumber = "12345678909"),
                    createdAt = LocalDateTime.now()
                )))

        //acao
        val response = grpcClient.consultar(ConsultarPixRequest.newBuilder()
            .setValorChave("12345678909")
            .build()
        )

        //validacao
        assertEquals("null", response.pixId)
        assertEquals("null", response.clienteId)
        assertEquals(KeyType.CPF, KeyType.valueOf(response.chave.tipoChave.name))
        assertEquals("12345678909", response.chave.valorChave)
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Test
    fun `Nao deve consultar chave pix por valorChave banco central do brasil`() {
        //cenario
        Mockito.`when`(bancoCentralClient.consultarChavePix(
            key = "12345678909"
        ))
            .thenReturn(
                HttpResponse.notFound())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(ConsultarPixRequest.newBuilder()
                .setValorChave("12345678909")
                .build()
            )
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix não encontrada", error.status.description)
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Test
    fun `Nao deve consultar chave pix quando parametros de entrada forem invalidos ou nao informados`() {
        //cenario

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(ConsultarPixRequest.newBuilder().build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix inválida ou não informada", error.status.description)
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Factory
    class GrpcClientFactory {
        @Singleton
        fun consultarChavePix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerConsultarGrpcServiceGrpc.KeyManagerConsultarGrpcServiceBlockingStub {
            return KeyManagerConsultarGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}