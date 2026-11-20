package cl.duoc.rinconsurapp.model

data class PrimerIngresoUiState(
    val listaPreguntas: List<String> = emptyList(),
    val preguntaSeleccionada: String = "",
    val respuestaSeguridad: String = "",
    val respuestaError: String? = null,
    val paso1Valido: Boolean = false,

    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val contrasenasVisibles: Boolean = false,
    val tieneLongitudValida: Boolean = false,
    val tieneNumero: Boolean = false,
    val tieneMayuscula: Boolean = false,
    val tieneEspecial: Boolean = false,
    val contrasenaError: String? = null,
    val paso2Valido: Boolean = false,

    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val errorGeneral: String? = null
)