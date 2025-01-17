package io.github.andreyfillipe.sistema_externo.banco_central

data class CadastrarChavePixBcbRequest (
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)