package io.github.andreyfillipe.sistema_externo.banco_central

data class DeletarChavePixBcbRequest(
    val key: String,
    val participant: String
) {
}