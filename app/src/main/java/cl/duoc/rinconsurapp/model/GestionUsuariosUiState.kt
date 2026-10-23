package cl.duoc.rinconsurapp.model

data class GestionUsuariosUiState(
    val listaUsuarios: List<Usuario> = emptyList(),
    val mostrarDialogoEliminar: Boolean = false,
    val usuarioAEliminar: Usuario? = null
)