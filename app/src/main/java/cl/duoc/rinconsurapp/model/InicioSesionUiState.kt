package cl.duoc.rinconsurapp.model

data class InicioSesionUiState(
    val rut: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val rutError: String? = null,
    val contrasenaError: String? = null,
    val errorGeneral: String? = null,
    val esValido: Boolean = false,
    val autenticado: Boolean = false
)