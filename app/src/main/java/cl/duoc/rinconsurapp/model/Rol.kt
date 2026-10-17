package cl.duoc.rinconsurapp.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        const val ID_OBSERVADOR = 1L
        const val ID_ADMIN = 2L
    }
}