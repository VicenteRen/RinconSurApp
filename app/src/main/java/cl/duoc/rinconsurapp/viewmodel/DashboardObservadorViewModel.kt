package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.Clasificacion
import cl.duoc.rinconsurapp.model.DashboardObservadorUiState
import cl.duoc.rinconsurapp.model.RangoEtario
import cl.duoc.rinconsurapp.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardObservadorViewModel(application: Application) :
    AndroidViewModel(application) {

    private val db = RinconSurDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(DashboardObservadorUiState())
    val uiState: StateFlow<DashboardObservadorUiState> = _uiState.asStateFlow()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        val usuario = db.buscarUsuarioPorId(sesion.leer())

        if (usuario?.rolId != Rol.ID_OBSERVADOR) {
            _uiState.value = DashboardObservadorUiState()
            return
        }

        val registros = db.listarRegistrosPorUsuario(usuario.id)

        val visitas = registros.count {
            it.clasificacion == Clasificacion.VISITA.etiqueta
        }

        val transito = registros.count {
            it.clasificacion == Clasificacion.TRANSITO.etiqueta
        }

        val noDeterminado = registros.count {
            it.clasificacion == Clasificacion.NO_DETERMINADO.etiqueta
        }

        val totalClasificados = visitas + transito

        val porcentajeVisitas = if (totalClasificados > 0) {
            visitas.toFloat() / totalClasificados * 100f
        } else {
            null
        }

        val porcentajeTransito = if (totalClasificados > 0) {
            transito.toFloat() / totalClasificados * 100f
        } else {
            null
        }

        val registrosPorEdad = RangoEtario.entries.associate {
                rango ->
            rango.etiqueta to registros.count {
                it.rangoEtario == rango.etiqueta
            }
        }

        _uiState.value = DashboardObservadorUiState(
            autorizado = true,
            totalRegistros = registros.size,
            totalVisitas = visitas,
            totalTransito = transito,
            totalNoDeterminado = noDeterminado,
            porcentajeVisitas = porcentajeVisitas,
            porcentajeTransito = porcentajeTransito,
            registrosPorEdad = registrosPorEdad
        )
    }

    override fun onCleared() {
        super.onCleared()
        db.close()
    }
}