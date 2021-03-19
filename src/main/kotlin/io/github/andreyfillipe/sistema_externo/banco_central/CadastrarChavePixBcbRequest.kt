package io.github.andreyfillipe.sistema_externo.banco_central

data class CadastrarChavePixBcbRequest (
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)

enum class KeyType {
    CPF, CNPJ, PHONE, EMAIL, RANDOM
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

enum class AccountType {
    CACC, SVGS
}

data class Owner (
    val type: Type,
    val name: String,
    val taxIdNumber: String
)

enum class Type {
    NATURAL_PERSON, LEGAL_PERSON
}