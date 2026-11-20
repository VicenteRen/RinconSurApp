
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

class HistorialAdminViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repositorio = ObservacionRepository()
    private val usuarioRepositorio = UsuarioRepository()
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

                val usuario = usuarioRepositorio.buscarPorId(idUsuario)

                if (usuario.rolId != Rol.ID_ADMIN || !usuario.activo) {
                    _autorizado.value = false
                    _registros.value = emptyList()
                    return@launch
                }

                _registros.value = repositorio.listarTodas()
                _autorizado.value = true

            } catch (e: Exception) {
                Log.e("HistorialAdmin", "Error al cargar registros", e)
            }
        }
    }


    fun anularRegistro(
        idRegistro: Long,
        motivo: String,
        contrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val idUsuario = sesion.leer()

                if (idUsuario == SesionPreferencias.SIN_SESION) {
                    onResultado(false, "Debes iniciar sesión.")
                    return@launch
                }

                val usuario = usuarioRepositorio.buscarPorId(idUsuario)

                if (usuario.rolId != Rol.ID_ADMIN || !usuario.activo) {
                    onResultado(false, "No tienes permisos para anular.")
                    return@launch
                }

                repositorio.anularObservacion(
                    id = idRegistro,
                    rut = usuario.rut,
                    contrasena = contrasena,
                    motivo = motivo
                )

                _registros.value = repositorio.listarTodas()

                onResultado(true, "Observación anulada correctamente.")

            } catch (e: Exception) {
                Log.e("HistorialAdmin", "Error al anular", e)

                onResultado(
                    false,
                    e.message ?: "No se pudo anular la observación."
                )
            }
        }
    }

}
