package cl.duoc.rinconsurapp.model

data class CrearUsuarioUiState(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val telefono: String = "",

    val rutError: String? = null,
    val correoError: String? = null,

    val listaRoles: List<String> = emptyList(),
    val rolSeleccionado: String = "",

    val listaPreguntas: List<String> = emptyList(),
    val preguntaSeleccionada: String = "",
    val respuestaSeguridad: String = "",
    val respuestaError: String? = null,

    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val contrasenaError: String? = null,
    val contrasenasVisibles: Boolean = false,

    val tieneLongitudValida: Boolean = false,
    val tieneNumero: Boolean = false,
    val tieneMayuscula: Boolean = false,
    val sinCaracteresEspeciales: Boolean = false,

    val errorGeneral: String? = null,
    val creacionExitosa: Boolean = false,
    val formularioValido: Boolean = false
)