package io.github.andreyfillipe.pix.cadastrar

import io.github.andreyfillipe.CadastrarPixRequest
import io.github.andreyfillipe.CadastrarPixResponse
import io.github.andreyfillipe.KeymanagerCadastrarGrpcServiceGrpc
import io.github.andreyfillipe.pix.Pix
import io.github.andreyfillipe.pix.toNovaChavePixRequest
import io.github.andreyfillipe.validacao.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CadastrarChavePixGrpcEndPoint(
    private val cadastrarChavePixService: CadastrarChavePixService
) : KeymanagerCadastrarGrpcServiceGrpc.KeymanagerCadastrarGrpcServiceImplBase() {

    override fun cadastrar(request: CadastrarPixRequest?,
                           responseObserver: StreamObserver<CadastrarPixResponse>?) {
        val novaChavePixRequest = request!!.toNovaChavePixRequest()
        val pix: Pix = this.cadastrarChavePixService.cadastrar(novaChavePixRequest)

        val response = CadastrarPixResponse
            .newBuilder()
            .setClienteId(pix.clienteId.toString())
            .setPixId(pix.id.toString())
            .build()

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}