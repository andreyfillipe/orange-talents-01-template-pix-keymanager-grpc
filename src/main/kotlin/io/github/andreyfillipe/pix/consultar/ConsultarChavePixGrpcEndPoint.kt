package io.github.andreyfillipe.pix.consultar

import com.google.protobuf.Timestamp
import io.github.andreyfillipe.*
import io.github.andreyfillipe.ConsultarPixResponse.*
import io.github.andreyfillipe.pix.PixRepository
import io.github.andreyfillipe.pix.toFiltro
import io.github.andreyfillipe.sistema_externo.banco_central.BancoCentralClient
import io.github.andreyfillipe.validacao.ErrorHandler
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultarChavePixGrpcEndPoint(
    private val pixRepository: PixRepository,
    private val bancoCentralClient: BancoCentralClient,
    private val validator: Validator
) : KeyManagerConsultarGrpcServiceGrpc.KeyManagerConsultarGrpcServiceImplBase() {

    override fun consultar(request: ConsultarPixRequest?,
                           responseObserver: StreamObserver<ConsultarPixResponse>?) {
        val filtro = request!!.toFiltro(validator)
        val pix = filtro.filtra(pixRepository = pixRepository, bancoCentralClient = bancoCentralClient)

        val response = newBuilder()
            .setPixId(pix.pixId.toString())
            .setClienteId(pix.clienteId.toString())
            .setChave(ChavePix
                .newBuilder()
                .setTipoChave(TipoChave.valueOf(pix.tipo.name))
                .setValorChave(pix.valorChave)
                .setConta(ChavePix.Conta.newBuilder()
                    .setTipoConta(TipoConta.valueOf(pix.tipoConta.name))
                    .setInstituicao(pix.conta.instituicao)
                    .setNomeTitular(pix.conta.nomeTitular)
                    .setCpfTitular(pix.conta.cpfTitular)
                    .setAgencia(pix.conta.agencia)
                    .setNumeroConta(pix.conta.numeroConta)
                    .build()
                )
                .setCriadoEm(pix.criadoEm.let {
                    val instant = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp
                        .newBuilder()
                        .setSeconds(instant.epochSecond)
                        .setNanos(instant.nano)
                        .build()
                })
            )
            .build()

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}