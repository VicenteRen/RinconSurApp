package cl.duoc.rinconsurapp.model

data class GestionUsuariosUiState(
    val listaUsuarios: List<Usuario> = emptyList(),
    val mostrarDialogoEliminar: Boolean = false,
    val usuarioAEliminar: Usuario? = null,
    val errorGlobal: String? = null,
    val mostrarDialogoEditar: Boolean = false,
    val usuarioAEditar: Usuario? = null,
    val editCorreo: String = "",
    val editTelefono: String = "",
    val editRolSeleccionado: String = "",
    val listaRoles: List<String> = listOf("Observador"),
    val editError: String? = null
)