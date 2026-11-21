package cl.duoc.rinconsurapp.model

enum class Clasificacion(val etiqueta: String) {
    TRANSITO("Tránsito"),
    VISITA("Visita"),
    NO_DETERMINADO("No determinado")
}

enum class RangoEtario(val etiqueta: String) {
    MENOR_18("Menor de 18 (acompañado)"),
    RANGO_18_25("18–25"),
    RANGO_26_40("26–40"),
    RANGO_41_60("41–60"),
    MAYOR_61("61 o más")
}

enum class Compania(val etiqueta: String) {
    SOLO("Solo"),
    PAREJA("Pareja / adulto acompañado"),
    CON_NINOS("Con niños"),
    GRUPO("Grupo (3 o más)")
}

data class Acceso(
    val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val activo: Boolean = true
) {
    val etiqueta: String
        get() = "$codigo - $nombre"
}

data class RegistroObservacionUiState(
    val nombreObservador: String = "Observador",
    val listaAccesos: List<Acceso> = emptyList(),
    val accesoDestino: Acceso? = null,
    val clasificacion: Clasificacion = Clasificacion.TRANSITO,
    val rangoEtario: RangoEtario = RangoEtario.RANGO_26_40,
    val compania: Compania = Compania.SOLO,
    val tieneComprasExternas: Boolean = false,
    val registroExitoso: Boolean = false,
    val mostrarBienvenida: Boolean = false,
    val cargando: Boolean = false
)

data class RegistroObservacion(
    val id: Long = 0,
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val tieneComprasExternas: Boolean,
    val accesoDestino: String?,
    val fechaHora: String = "",
    val usuarioId: Long,
    val nombreObservador: String = ""
)