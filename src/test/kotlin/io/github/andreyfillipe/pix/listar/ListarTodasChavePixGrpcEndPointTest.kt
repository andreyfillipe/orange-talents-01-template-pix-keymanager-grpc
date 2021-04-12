package io.github.andreyfillipe.pix.listar

import io.github.andreyfillipe.KeyManagerListarGrpcServiceGrpc
import io.github.andreyfillipe.ListarPixRequest
import io.github.andreyfillipe.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton
import java.util.ArrayList

@MicronautTest(transactional = false)
internal class ListarTodasChavePixGrpcEndPointTest(
    val pixRepository: PixRepository,
    val grpcClient: KeyManagerListarGrpcServiceGrpc.KeyManagerListarGrpcServiceBlockingStub
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        pixRepository.deleteAll()
    }

    @Test
    fun `Deve listar todas chaves pix`() {
        //cenario
        val pixList: MutableList<Pix> = ArrayList()
        for (i in 1..5) {
            pixList.add(Pix(
                clienteId = CLIENTE_ID,
                tipoChave = TipoChave.CPF,
                valorChave = "1234567890${i}",
                tipoConta = TipoConta.CONTA_CORRENTE,
                conta = Conta(
                    instituicao = "",
                    nomeTitular = "",
                    cpfTitular = "",
                    agencia = "",
                    numeroConta = ""
                )
            ))
        }
        pixRepository.saveAll(pixList)

        //acao
        val response = grpcClient.listar(ListarPixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .build()
        )

        //validacao
        assertEquals(CLIENTE_ID.toString(), response.clienteId)
        assertEquals(pixList[0].id.toString(), response.chavePixList[0].pixId)
        assertEquals(TipoChave.CPF, TipoChave.valueOf(response.chavePixList[0].tipoChave.name))
        assertEquals("12345678901", response.chavePixList[0].valorChave)
        assertEquals(TipoConta.CONTA_CORRENTE, TipoConta.valueOf(response.chavePixList[0].tipoConta.name))
        assertTrue(response.chavePixCount == 5)
    }

    @Test
    fun `Nao deve listar todas chaves pix quando parametros de entrada forem invalidos`() {
        //cenario

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.listar(ListarPixRequest.newBuilder().build())
        }

        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Cliente ID obrigatório", error.status.description)
    }

    @Factory
    class GrpcClientFactory {
        @Singleton
        fun listarChavePix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListarGrpcServiceGrpc.KeyManagerListarGrpcServiceBlockingStub {
            return KeyManagerListarGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}