package io.github.andreyfillipe.pix.cadastrar

import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.validator.constraints.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

@Introspected
enum class TipoChaveRequest {
    CPF {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()) {
                return false
            }

            if(!chave.matches("^[0-9]{11}\$".toRegex())) {
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },

    CELULAR {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()) {
                return false
            }

            return chave.matches("^\\+[1-9]{1}[0-9]{12}\$".toRegex())
        }

    },

    EMAIL {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()) {
                return false
            }

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }

    },

    CHAVE_ALEATORIA{
        override fun valida(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }

    };

    abstract fun valida(chave: String?) : Boolean
}