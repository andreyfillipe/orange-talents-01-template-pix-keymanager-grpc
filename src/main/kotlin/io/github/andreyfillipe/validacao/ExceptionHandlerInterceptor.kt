package io.github.andreyfillipe.validacao

import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.github.andreyfillipe.validacao.handlers.ApiErroException
import io.grpc.BindableService
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ExceptionHandlerInterceptor(
    private val resolver: ExceptionHandlerResolver
) : MethodInterceptor<BindableService, Any> {
    override fun intercept(context: MethodInvocationContext<BindableService, Any>): Any? {
        return try {
            context.proceed()
        } catch (e: Exception) {
            val statusError = when (e) {
                is ApiErroException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is ConstraintViolationException -> handleConstraintValidationException(e)
                else -> Status.UNKNOWN.withDescription("unexpected error happened").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null

            /*val handler = resolver.resolve(e)
            val status = handler.handle(e)

            val observer = context.parameterValues[1] as StreamObserver<*>
            observer.onError(status.asRuntimeException())
            null*/
        }
    }

    private fun handleConstraintValidationException(e: ConstraintViolationException): StatusRuntimeException {
        val badRequest = BadRequest.newBuilder() // com.google.rpc.BadRequest
            .addAllFieldViolations(e.constraintViolations.map {
                BadRequest.FieldViolation.newBuilder()
                    .setField(it.propertyPath.last().name) // propertyPath=save.entity.email
                    .setDescription(it.message)
                    .build()
            }
            ).build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("request with invalid parameters")
            .addDetails(com.google.protobuf.Any.pack(badRequest)) // com.google.protobuf.Any
            .build()

        return StatusProto.toStatusRuntimeException(statusProto) // io.grpc.protobuf.StatusProto
    }
}