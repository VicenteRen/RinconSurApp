
package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.ObservacionRepository
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.RegistroObservacion
import cl.duoc.rinconsurapp.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistorialObservadorViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repositorio = ObservacionRepository()
    private val usuarios = UsuarioRepository()
    private val sesion = SesionPreferencias(application)

    private val _registros =
        MutableStateFlow<List<RegistroObservacion>>(emptyList())
    val registros: StateFlow<List<RegistroObservacion>> =
        _registros.asStateFlow()

    private val _autorizado = MutableStateFlow(false)
    val autorizado: StateFlow<Boolean> =
        _autorizado.asStateFlow()

    init {
        cargarRegistros()
    }

    fun cargarRegistros() {
        viewModelScope.launch {
            try {
                val idUsuario = sesion.leer()

                if (idUsuario == SesionPreferencias.SIN_SESION) {
                    _autorizado.value = false
                    _registros.value = emptyList()
                    return@launch
                }

                val usuario = usuarios.buscarPorId(idUsuario)

                if (usuario.rolId != Rol.ID_OBSERVADOR || !usuario.activo) {
                    _autorizado.value = false
                    _registros.value = emptyList()
                    return@launch
                }

                _registros.value = repositorio.listarPorUsuario(idUsuario)
                _autorizado.value = true

            } catch (e: Exception) {
                Log.e(
                    "HistorialObservador",
                    "Error al cargar observaciones",
                    e
                )
            }
        }
    }
}
