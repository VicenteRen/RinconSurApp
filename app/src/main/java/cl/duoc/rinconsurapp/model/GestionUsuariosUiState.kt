package cl.duoc.rinconsurapp.model

data class GestionUsuariosUiState(
    val listaUsuarios: List<Usuario> = emptyList(),
    // Estado para Eliminar
    val mostrarDialogoEliminar: Boolean = false,
    val usuarioAEliminar: Usuario? = null,
    val errorGlobal: String? = null,
    // Estado para Editar
    val mostrarDialogoEditar: Boolean = false,
    val usuarioAEditar: Usuario? = null,
    val editCorreo: String = "",
    val editTelefono: String = "",
    val editRolSeleccionado: String = "",
    val listaRoles: List<String> = listOf("Observador"), // <-- Solo observador por ahora. A futuro se añaden los 3 más aquí.
    val editError: String? = null
)