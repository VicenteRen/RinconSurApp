package cl.duoc.rinconsurapp.model

data class RestablecerContrasenaUiState(
    val rut: String = "",
    val rutError: String? = null,

    val listaPreguntas: List<String> = listOf(
        "¿Cuál es el nombre de tu primera mascota?",
        "¿Cuál es el nombre de tu amigo de la infancia?",
        "¿En qué ciudad se conocieron tus padres?"
    ),
    val preguntaSeleccionada: String = "¿Cuál es el nombre de tu primera mascota?",

    val respuestaSeguridad: String = "",
    val respuestaError: String? = null,

    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val contrasenaError: String? = null,

    val contrasenasVisibles: Boolean = false,

    val tieneLongitudValida: Boolean = false,
    val tieneNumero: Boolean = false,
    val tieneMayuscula: Boolean = false,
    val sinCaracteresEspeciales: Boolean = false,

    val errorGeneral: String? = null,
    val restablecimientoExitoso: Boolean = false,
    val formularioValido: Boolean = false
)