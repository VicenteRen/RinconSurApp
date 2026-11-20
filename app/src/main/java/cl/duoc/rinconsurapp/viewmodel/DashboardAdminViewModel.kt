
package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.ObservacionRepository
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.Clasificacion
import cl.duoc.rinconsurapp.model.DashboardAdminUiState
import cl.duoc.rinconsurapp.model.RangoEtario
import cl.duoc.rinconsurapp.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardAdminViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val observaciones = ObservacionRepository()
    private val usuarios = UsuarioRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(DashboardAdminUiState())
    val uiState: StateFlow<DashboardAdminUiState> =
        _uiState.asStateFlow()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                val idUsuario = sesion.leer()

                if (idUsuario == SesionPreferencias.SIN_SESION) {
                    _uiState.value = DashboardAdminUiState()
                    return@launch
                }

                val usuario = usuarios.buscarPorId(idUsuario)

                if (usuario.rolId != Rol.ID_ADMIN || !usuario.activo) {
                    _uiState.value = DashboardAdminUiState()
                    return@launch
                }

                val registros = observaciones.listarTodas()

                val transito = registros.count {
                    it.clasificacion == Clasificacion.TRANSITO.etiqueta
                }

                val visitas = registros.count {
                    it.clasificacion == Clasificacion.VISITA.etiqueta
                }

                val noDeterminado = registros.count {
                    it.clasificacion == Clasificacion.NO_DETERMINADO.etiqueta
                }

                val total = transito + visitas + noDeterminado

                val porcentajeTransito = if (total > 0) {
                    transito.toFloat() / total * 100f
                } else {
                    null
                }

                val registrosPorEdad = RangoEtario.entries.associate { rango ->
                    rango.etiqueta to registros.count {
                        it.rangoEtario == rango.etiqueta
                    }
                }

                _uiState.value = DashboardAdminUiState(
                    autorizado = true,
                    totalRegistros = registros.size,
                    totalTransito = transito,
                    totalVisitas = visitas,
                    totalNoDeterminado = noDeterminado,
                    porcentajeTransito = porcentajeTransito,
                    registrosPorEdad = registrosPorEdad
                )

            } catch (e: Exception) {
                Log.e(
                    "DashboardAdmin",
                    "Error al consultar los microservicios",
                    e
                )
            }
        }
    }
}
