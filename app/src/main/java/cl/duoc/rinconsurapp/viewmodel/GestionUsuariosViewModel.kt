package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.model.GestionUsuariosUiState
import cl.duoc.rinconsurapp.model.Rol
import cl.duoc.rinconsurapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GestionUsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RinconSurDbHelper(application)
    private val _uiState = MutableStateFlow(GestionUsuariosUiState())
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        val usuariosActivos = db.listarUsuariosActivos()
        _uiState.update { it.copy(listaUsuarios = usuariosActivos) }
    }

    // --- ELIMINAR ---
    fun confirmarEliminacion(usuario: Usuario) {
        // Regla de Negocio: No eliminar al último administrador
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
        _uiState.value.usuarioAEliminar?.let { usuario ->
            db.eliminarUsuario(usuario.id)
            cargarUsuarios()
        }
        cancelarEliminacion()
    }

    fun limpiarErrorGlobal() {
        _uiState.update { it.copy(errorGlobal = null) }
    }

    // --- EDITAR ---
    fun abrirDialogoEditar(usuario: Usuario) {
        _uiState.update {
            it.copy(
                mostrarDialogoEditar = true,
                usuarioAEditar = usuario,
                editCorreo = usuario.correo,
                editTelefono = usuario.telefono,
                editRolSeleccionado = usuario.rolNombre,
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
        _uiState.update { it.copy(editCorreo = correo.lowercase()) }
    }

    fun onEditTelefonoCambiado(telefono: String) {
        val telefonoLimpio = telefono.filter { it.isDigit() }
        if (telefonoLimpio.length > 9) return
        _uiState.update { it.copy(editTelefono = telefonoLimpio) }
    }

    fun onEditRolSeleccionado(rol: String) {
        _uiState.update { it.copy(editRolSeleccionado = rol) }
    }

    fun guardarEdicion() {
        val state = _uiState.value
        val usuario = state.usuarioAEditar ?: return

        if (state.editCorreo.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.editCorreo).matches()) {
            _uiState.update { it.copy(editError = "Correo inválido") }
            return
        }

        if (state.editTelefono.length < 9) {
            _uiState.update { it.copy(editError = "El teléfono debe tener 9 dígitos") }
            return
        }

        // Regla de Negocio: No se puede cambiar a un Administrador a otro rol
        if (usuario.rolId == Rol.ID_ADMIN && state.editRolSeleccionado != "Administrador") {
            _uiState.update { it.copy(editError = "No está permitido cambiar el rol de un Administrador.") }
            return
        }

        val rolId = if (state.editRolSeleccionado == "Administrador") Rol.ID_ADMIN else Rol.ID_OBSERVADOR

        try {
            db.actualizarUsuarioDesdeAdmin(usuario.id, state.editCorreo, state.editTelefono, rolId)
            cargarUsuarios()
            cancelarEdicion()
        } catch (e: Exception) {
            _uiState.update { it.copy(editError = "Error al actualizar la base de datos") }
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}