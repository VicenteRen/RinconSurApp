package cl.duoc.rinconsurapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.GestionUsuariosUiState
import cl.duoc.rinconsurapp.model.Rol
import cl.duoc.rinconsurapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GestionUsuariosViewModel : ViewModel() {

    companion object {
        private val ROLES_DISPONIBLES = listOf("Observador", "Administrador")
        private val PATRON_CORREO = Regex("^[a-z0-9ñáéíóúü._%+-]+@[a-z0-9ñáéíóúü.-]+\\.[a-z]{2,6}$")
    }

    private val repositorio = UsuarioRepository()
    private val _uiState = MutableStateFlow(GestionUsuariosUiState(listaRoles = ROLES_DISPONIBLES))
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            try {
                val usuariosActivos = repositorio.listarActivos()
                _uiState.update { it.copy(listaUsuarios = usuariosActivos, listaRoles = ROLES_DISPONIBLES) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGlobal = e.message ?: "Error al cargar usuarios") }
            }
        }
    }

    fun confirmarEliminacion(usuario: Usuario) {
        if (usuario.rolId == Rol.ID_ADMIN) {
            val cantidadAdmins = _uiState.value.listaUsuarios.count { it.rolId == Rol.ID_ADMIN }
            if (cantidadAdmins <= 1) {
                _uiState.update { it.copy(errorGlobal = "Operación denegada: No puedes eliminar al único Administrador activo del sistema.") }
                return
            }
        }
        _uiState.update { it.copy(mostrarDialogoEliminar = true, usuarioAEliminar = usuario) }
    }

    fun cancelarEliminacion() {
        _uiState.update { it.copy(mostrarDialogoEliminar = false, usuarioAEliminar = null) }
    }

    fun eliminarUsuario() {
        val usuario = _uiState.value.usuarioAEliminar ?: return
        cancelarEliminacion()
        viewModelScope.launch {
            try {
                repositorio.eliminarUsuario(usuario.id)
                cargarUsuarios()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGlobal = e.message ?: "Error al eliminar usuario") }
            }
        }
    }

    fun limpiarErrorGlobal() {
        _uiState.update { it.copy(errorGlobal = null) }
    }

    fun abrirDialogoEditar(usuario: Usuario) {
        _uiState.update {
            it.copy(
                mostrarDialogoEditar = true,
                usuarioAEditar = usuario,
                editCorreo = usuario.correo,
                editTelefono = usuario.telefono,
                editRolSeleccionado = usuario.rolNombre,
                listaRoles = ROLES_DISPONIBLES,
                editError = null
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update {
            it.copy(mostrarDialogoEditar = false, usuarioAEditar = null, editError = null)
        }
    }

    fun onEditCorreoCambiado(correo: String) {
        _uiState.update { it.copy(editCorreo = correo.lowercase(), editError = null) }
    }

    fun onEditTelefonoCambiado(telefono: String) {
        val telefonoLimpio = telefono.filter { it.isDigit() }
        if (telefonoLimpio.length > 9) return
        _uiState.update { it.copy(editTelefono = telefonoLimpio, editError = null) }
    }

    fun onEditRolSeleccionado(rol: String) {
        _uiState.update { it.copy(editRolSeleccionado = rol, editError = null) }
    }

    fun guardarEdicion() {
        val state = _uiState.value
        val usuario = state.usuarioAEditar ?: return

        val correoLimpio = state.editCorreo.trim()
        if (correoLimpio.isBlank() || !correoLimpio.matches(PATRON_CORREO)) {
            _uiState.update { it.copy(editError = "Correo inválido") }
            return
        }

        if (state.editTelefono.length < 9) {
            _uiState.update { it.copy(editError = "El teléfono debe tener 9 dígitos") }
            return
        }

        if (usuario.rolId == Rol.ID_ADMIN && state.editRolSeleccionado != "Administrador") {
            _uiState.update { it.copy(editError = "No está permitido cambiar el rol de un Administrador.") }
            return
        }

        val rolId = if (state.editRolSeleccionado == "Administrador") Rol.ID_ADMIN else Rol.ID_OBSERVADOR

        viewModelScope.launch {
            try {
                repositorio.actualizarDesdeAdmin(usuario.id, correoLimpio, state.editTelefono, rolId)
                cargarUsuarios()
                cancelarEdicion()
            } catch (e: Exception) {
                _uiState.update { it.copy(editError = e.message ?: "Error al actualizar usuario") }
            }
        }
    }
}