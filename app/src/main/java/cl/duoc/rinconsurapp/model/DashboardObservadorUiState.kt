package cl.duoc.rinconsurapp.model

data class DashboardObservadorUiState(
    val autorizado: Boolean = false,
    val totalRegistros: Int = 0,
    val totalVisitas: Int = 0,
    val totalTransito: Int = 0,
    val totalNoDeterminado: Int = 0,
    val porcentajeVisitas: Float? = null,
    val porcentajeTransito: Float? = null,
    val registrosPorEdad: Map<String, Int> = emptyMap()
)