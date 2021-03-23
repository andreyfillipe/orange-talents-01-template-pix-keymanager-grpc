package io.github.andreyfillipe.sistema_externo.banco_central

import io.github.andreyfillipe.pix.Conta
import io.github.andreyfillipe.pix.TipoChave
import io.github.andreyfillipe.pix.TipoConta
import io.github.andreyfillipe.pix.consultar.ConsultarChavePixResponse
import java.time.LocalDateTime

data class CadastrarChavePixBcbResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
    fun toConsultarChavePixResponse(): ConsultarChavePixResponse {
        return ConsultarChavePixResponse(
            tipo = when(this.keyType) {
                KeyType.CPF -> TipoChave.CPF
                KeyType.CNPJ -> TipoChave.CPF
                KeyType.PHONE -> TipoChave.CELULAR
                KeyType.EMAIL -> TipoChave.EMAIL
                KeyType.RANDOM -> TipoChave.CHAVE_ALEATORIA
            },
            valorChave = this.key,
            tipoConta = when(this.bankAccount.accountType) {
                AccountType.CACC -> TipoConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoConta.CONTA_POUPANCA
            },
            conta = Conta(
                instituicao = bankAccount.participant,
                nomeTitular = owner.name,
                cpfTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroConta = bankAccount.accountNumber
            )
        )
    }
}