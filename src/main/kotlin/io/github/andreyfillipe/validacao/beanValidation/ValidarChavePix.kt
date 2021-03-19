package io.github.andreyfillipe.validacao.beanValidation

import io.github.andreyfillipe.pix.cadastrar.NovaChavePixRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidarChavePixValidator::class])
annotation class ValidarChavePix(
    val message: String = "Chave Pix inválida",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidarChavePixValidator : ConstraintValidator<ValidarChavePix, NovaChavePixRequest> {

    override fun isValid(
        value: NovaChavePixRequest,
        annotationMetadata: AnnotationValue<ValidarChavePix>,
        context: ConstraintValidatorContext
    ): Boolean {

        if (value.tipoChave == null) {
            return false
        }

        return value.tipoChave.valida(value.valorChave)
    }

}