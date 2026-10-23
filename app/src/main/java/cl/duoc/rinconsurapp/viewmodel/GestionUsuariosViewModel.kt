package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.model.GestionUsuariosUiState
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

    fun confirmarEliminacion(usuario: Usuario) {
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

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}