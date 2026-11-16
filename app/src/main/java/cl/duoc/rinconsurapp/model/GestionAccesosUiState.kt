package cl.duoc.rinconsurapp.model

data class GestionAccesosUiState(
    val codigo: String = "",
    val nombre: String = "",
    val errorCodigo: String? = null,
    val errorNombre: String? = null,
    val listaAccesos: List<Acceso> = emptyList(),
    val cargando: Boolean = false
)