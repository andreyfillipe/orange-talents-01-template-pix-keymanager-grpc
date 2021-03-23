package io.github.andreyfillipe.pix.excluir

import io.github.andreyfillipe.ExcluirPixRequest
import io.github.andreyfillipe.ExcluirPixResponse
import io.github.andreyfillipe.KeyManagerExcluirGrpcServiceGrpc
import io.github.andreyfillipe.pix.toExcluirChavePixRequest
import io.github.andreyfillipe.validacao.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ExcluirChavePixGrpcEndPoint(
    private val excluirChavePixService: ExcluirChavePixService
) : KeyManagerExcluirGrpcServiceGrpc.KeyManagerExcluirGrpcServiceImplBase() {

    override fun excluir(request: ExcluirPixRequest?,
                         responseObserver: StreamObserver<ExcluirPixResponse>?) {
        val excluirChavePixRequest = request!!.toExcluirChavePixRequest()
        excluirChavePixService.excluir(excluirChavePixRequest)

        val response = ExcluirPixResponse
            .newBuilder()
            .setPixId(request.pixId)
            .setClienteId(request.clienteId)
            .build()

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}