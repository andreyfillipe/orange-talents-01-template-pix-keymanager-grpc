package io.github.andreyfillipe.pix.listar

import com.google.protobuf.Timestamp
import io.github.andreyfillipe.*
import io.github.andreyfillipe.pix.PixRepository
import io.github.andreyfillipe.validacao.ErrorHandler
import io.github.andreyfillipe.validacao.handlers.ApiErroException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListarTodasChavePixGrpcEndPoint(
    private val pixRepository: PixRepository
) : KeyManagerListarGrpcServiceGrpc.KeyManagerListarGrpcServiceImplBase() {

    override fun listar(request: ListarPixRequest?, responseObserver: StreamObserver<ListarPixResponse>?) {
        if (request!!.clienteId.isNullOrEmpty()) {
            throw ApiErroException(Status.NOT_FOUND, "Cliente ID obrigatório")
        }
        val clienteId = UUID.fromString(request.clienteId)
        val pix = pixRepository.findAllByClienteId(clienteId)

        val chavesPixResponse = pix.map {
            ListarPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setValorChave(it.valorChave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setCriadoEm(it.criadoEm.let {
                    val instant = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp
                        .newBuilder()
                        .setSeconds(instant.epochSecond)
                        .setNanos(instant.nano)
                        .build()
                })
                .build()
        }

        val response = ListarPixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChavePix(chavesPixResponse)
            .build()

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}