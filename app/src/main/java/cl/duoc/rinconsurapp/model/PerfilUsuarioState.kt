package cl.duoc.rinconsurapp.model

data class PerfilUsuarioUiState(
    val nombreCompleto: String = "Vicente Ponce",
    val rut: String = "214297353",
    val cargo: String = "Observador", // Una sola variable que cambiará dinámicamente

    // Campos editables
    val telefonoContacto: String = "987654321",
    val correoContacto: String = "v.ponce@rinconsur.cl",

    // Validaciones
    val errorTelefono: String? = null,
    val errorCorreo: String? = null,
    val esValido: Boolean = true,

    val informacionModificadaExitosamente: Boolean = false,
    val cerrarSesionExitoso: Boolean = false
)