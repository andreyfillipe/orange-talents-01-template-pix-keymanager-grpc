package io.github.andreyfillipe.sistema_externo.banco_central

import io.micronaut.context.annotation.Parameter
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${banco.central.url}")
interface BancoCentralClient {

    @Post("/api/v1/pix/keys")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun cadastrarChavePix(@Body request: CadastrarChavePixBcbRequest) : HttpResponse<CadastrarChavePixBcbResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun excluirChavePix(@Parameter key: String, @Body request: DeletarChavePixBcbRequest) : HttpResponse<DeletarChavePixBcbResponse>

    @Get("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun consultarChavePix(@Parameter key: String) : HttpResponse<ConsultarChavePixBcbResponse>
}