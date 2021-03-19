package io.github.andreyfillipe.pix

import javax.persistence.Embeddable

@Embeddable
class Conta(
    val instituicao: String,
    val nomeTitular: String,
    val cpfTitular: String,
    val agencia: String,
    val numeroConta: String
){
    fun isbp(): String = "60701190"
}