package io.github.andreyfillipe.sistema_externo.banco_central

import java.time.LocalDateTime

data class DeletarChavePixBcbResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
){
}