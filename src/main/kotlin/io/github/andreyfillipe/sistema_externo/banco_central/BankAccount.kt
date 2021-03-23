package io.github.andreyfillipe.sistema_externo.banco_central

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

enum class AccountType {
    CACC, SVGS
}