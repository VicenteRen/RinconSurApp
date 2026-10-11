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

enum class Acceso(val etiqueta: String) {
    A1("A1 - Acceso Principal"),
    A2("A2 - Acceso Lateral"),
    A3("A3 - Acceso Estacionamiento"),
    A4("A4 - Acceso Conexión")
}

data class RegistroObservacionUiState(
    val clasificacion: Clasificacion = Clasificacion.TRANSITO,
    val rangoEtario: RangoEtario = RangoEtario.RANGO_26_40,
    val compania: Compania = Compania.SOLO,
    val tieneComprasExternas: Boolean = false,
    val accesoDestino: Acceso? = null,
    val registroExitoso: Boolean = false
)