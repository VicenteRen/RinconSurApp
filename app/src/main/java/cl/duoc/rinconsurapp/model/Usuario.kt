package cl.duoc.rinconsurapp.model

data class Usuario(
    val id: Long = 0,
    val rut: String,
    val contrasena: String,
    val nombreCompleto: String,
    val correo: String,
    val telefono: String,
    val rolId: Long = Rol.ID_OBSERVADOR,
    val rolNombre: String = "",
    val avatarUrl: String? = null,
    val primerIngreso: Boolean = false,
    val activo: Boolean = true
)