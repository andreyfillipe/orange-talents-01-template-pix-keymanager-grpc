package io.github.andreyfillipe.sistema_externo.itau

import io.github.andreyfillipe.pix.Conta

data class DetalhesContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toConta(): Conta {
        return Conta(
            instituicao = this.instituicao.nome,
            nomeTitular = this.titular.nome,
            cpfTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroConta = this.numero
        )
    }
}

data class InstituicaoResponse(
    val nome: String,
    val ispb: String
) {
}

data class TitularResponse(
   val id: String,
   val nome: String,
   val cpf: String
) {
}