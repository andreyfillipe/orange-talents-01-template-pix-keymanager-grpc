package io.github.andreyfillipe.sistema_externo.banco_central

import java.time.LocalDateTime

data class ConsultarChavePixBcbResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
}