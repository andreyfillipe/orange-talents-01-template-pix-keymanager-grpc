package io.github.andreyfillipe.pix.cadastrar

import com.google.rpc.BadRequest
import io.github.andreyfillipe.CadastrarPixRequest
import io.github.andreyfillipe.KeyManagerCadastrarGrpcServiceGrpc
import io.github.andreyfillipe.TipoChave
import io.github.andreyfillipe.TipoConta
import io.github.andreyfillipe.pix.Conta
import io.github.andreyfillipe.pix.Pix
import io.github.andreyfillipe.pix.PixRepository
import io.github.andreyfillipe.sistema_externo.banco_central.*
import io.github.andreyfillipe.sistema_externo.itau.DetalhesContaResponse
import io.github.andreyfillipe.sistema_externo.itau.InstituicaoResponse
import io.github.andreyfillipe.sistema_externo.itau.ItauClient
import io.github.andreyfillipe.sistema_externo.itau.TitularResponse
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
internal class CadastrarChavePixGrpcEndPointTest(
    val pixRepository: PixRepository,
    val grpcClient: KeyManagerCadastrarGrpcServiceGrpc.KeyManagerCadastrarGrpcServiceBlockingStub
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
    fun `Deve salvar nova chave pix`() {
        //cenario
        Mockito.`when`(itauClient.buscarContaPorChaveETipo(
            clienteId = CLIENTE_ID.toString(),
            tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(DetalhesContaResponse(
                tipo = "CONTA_CORRENTE",
                instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
                agencia = "0001",
                numero = "123456",
                titular = TitularResponse("c56dfef4-7901-44fb-84e2-a2cefb157890", "Nome do Titular", "12345678909")
            )))

        Mockito.`when`(bancoCentralClient.cadastrarChavePix(CadastrarChavePixBcbRequest(
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
                taxIdNumber = "12345678909")
        )))
            .thenReturn(HttpResponse.created(CadastrarChavePixBcbResponse(
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
        val response = grpcClient.cadastrar(
            CadastrarPixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.CPF)
                .setValorChave("12345678909")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        //validacao
        assertNotNull(response.clienteId)
        assertNotNull(response.pixId)
        assertEquals(CLIENTE_ID.toString(), response.clienteId)
        assertTrue(pixRepository.existsById(UUID.fromString(response.pixId)))
    }

    @Test
    fun `Nao deve salvar nova chave pix quando pix ja existir`() {
        //cenario
        val pix = Pix(
            clienteId = CLIENTE_ID,
            tipoChave = io.github.andreyfillipe.pix.TipoChave.CPF,
            valorChave = "12345678909",
            tipoConta = io.github.andreyfillipe.pix.TipoConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "",
                nomeTitular = "",
                cpfTitular = "",
                agencia = "",
                numeroConta = ""
            )
        )
        pixRepository.save(pix)

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(
                CadastrarPixRequest.newBuilder()
                    .setClienteId(pix.clienteId.toString())
                    .setTipoChave(TipoChave.valueOf(pix.tipoChave.name))
                    .setValorChave(pix.valorChave)
                    .setTipoConta(TipoConta.valueOf(pix.tipoConta.name))
                    .build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix: ${pix.valorChave} já existe", error.status.description)
        assertTrue(pixRepository.findAll().count() == 1)
    }

    @Test
    fun `Nao deve salvar nova chave pix quando clienteId nao existir no banco Itau`() {
        //cenario
        Mockito.`when`(itauClient.buscarContaPorChaveETipo(
            clienteId = CLIENTE_ID.toString(),
            tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(
                CadastrarPixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("12345678909")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Cliente não encontrado no Itaú", error.status.description)
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Test
    fun `Nao deve salvar nova chave pix quando ocorrer erro no Banco Central`() {
        //cenario
        Mockito.`when`(itauClient.buscarContaPorChaveETipo(
            clienteId = CLIENTE_ID.toString(),
            tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(DetalhesContaResponse(
                tipo = "CONTA_CORRENTE",
                instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
                agencia = "0001",
                numero = "123456",
                titular = TitularResponse("c56dfef4-7901-44fb-84e2-a2cefb157890", "Nome do Titular", "12345678909")
            )))

        Mockito.`when`(bancoCentralClient.cadastrarChavePix(CadastrarChavePixBcbRequest(
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
                taxIdNumber = "12345678909")
        )))
            .thenReturn(HttpResponse.badRequest())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(
                CadastrarPixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("12345678909")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Erro ao cadastrar chave Pix no Banco Central do Brasil", error.status.description)
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Test
    fun `Nao deve salvar nova chave pix quando parametros de entrada forem invalidos`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(CadastrarPixRequest.newBuilder().build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("request with invalid parameters", error.status.description)
        MatcherAssert.assertThat(error.violations(), Matchers.containsInAnyOrder(
            Pair("request", "Chave Pix inválida"),
            Pair("clienteId", "must not be blank"),
            Pair("tipoChave", "must not be null"),
            Pair("valorChave", "must not be blank"),
            Pair("tipoConta", "must not be null")
        ))
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Test
    fun `Nao deve salvar nova chave pix quando valorChave for maior que 77 caracteres`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(
                CadastrarPixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("12345678901234567890123456789012345678901234567890123456789012345678901234567890")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("request with invalid parameters", error.status.description)
        MatcherAssert.assertThat(error.violations(), Matchers.containsInAnyOrder(
            Pair("request", "Chave Pix inválida"),
            Pair("valorChave", "size must be between 0 and 77")
        ))
        assertTrue(pixRepository.findAll().count() == 0)
    }

    @Factory
    class GrpcClientFactory {
        @Singleton
        fun cadastrarChavePix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerCadastrarGrpcServiceGrpc.KeyManagerCadastrarGrpcServiceBlockingStub {
            return KeyManagerCadastrarGrpcServiceGrpc.newBlockingStub(channel)
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