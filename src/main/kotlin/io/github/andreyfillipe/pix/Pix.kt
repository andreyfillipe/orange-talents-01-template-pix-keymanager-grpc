package io.github.andreyfillipe.pix

import io.github.andreyfillipe.sistema_externo.banco_central.*
import io.github.andreyfillipe.sistema_externo.banco_central.Type.*
import io.github.andreyfillipe.validacao.beanValidation.ValidarUUID
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class Pix(
    @ValidarUUID
    @field:NotNull
    @Column(nullable = false)
    @Type(type = "org.hibernate.type.UUIDCharType")
    val clienteId: UUID,
    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,
    @field:NotBlank
    @field:Size(max = 77)
    @Column(nullable = false, unique = true)
    var valorChave: String,
    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,
    @field:Valid
    @Embedded
    val conta: Conta
) {
    @Id
    @GeneratedValue
    @Type(type = "org.hibernate.type.UUIDCharType")
    var id: UUID? = null
    @field:NotNull
    @Column(nullable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()

    fun toCadastrarChavePixBcbRequest(): CadastrarChavePixBcbRequest {
        return CadastrarChavePixBcbRequest(
            keyType = when(tipoChave) {
                TipoChave.CPF -> KeyType.CPF
                TipoChave.CELULAR -> KeyType.PHONE
                TipoChave.EMAIL -> KeyType.EMAIL
                TipoChave.CHAVE_ALEATORIA -> KeyType.RANDOM
            },
            key = valorChave,
            BankAccount(
                participant = conta.isbp(),
                branch = conta.agencia,
                accountNumber = conta.numeroConta,
                accountType = when(tipoConta) {
                    TipoConta.CONTA_CORRENTE -> AccountType.CACC
                    else -> AccountType.SVGS
                }
            ),
            Owner(
                type = NATURAL_PERSON,
                name = conta.nomeTitular,
                taxIdNumber = conta.cpfTitular
            )
        )
    }
}