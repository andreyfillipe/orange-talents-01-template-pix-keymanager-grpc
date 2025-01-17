package io.github.andreyfillipe.pix

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository : JpaRepository<Pix, UUID> {

    @Query("select case when count(p) > 0 then true else false end from Pix p where p.valorChave = :valorChave")
    fun existsByValorChave(valorChave: String): Boolean
    fun findByValorChave(valorChave: String): Optional<Pix>
    fun findByIdAndClienteId(id: UUID, clienteId: UUID): Optional<Pix>
    fun findAllByClienteId(clienteId: UUID): List<Pix>
}