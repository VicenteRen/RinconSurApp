package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.Clasificacion
import cl.duoc.rinconsurapp.model.DashboardAdminUiState
import cl.duoc.rinconsurapp.model.RangoEtario
import cl.duoc.rinconsurapp.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DashboardAdminViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = RinconSurDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(DashboardAdminUiState())
    val uiState: StateFlow<DashboardAdminUiState> =
        _uiState.asStateFlow()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        val usuario = db.buscarUsuarioPorId(sesion.leer())

        if (usuario?.rolId != Rol.ID_ADMIN) {
            _uiState.value = DashboardAdminUiState()
            return
        }

        val registros = db.listarTodosLosRegistros()

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

        val porcentaje = if (total > 0) {
            transito.toFloat() / total * 100f
        } else {
            null
        }

        val porEdad = RangoEtario.entries.associate { rango ->
            rango.etiqueta to registros.count {
                it.rangoEtario == rango.etiqueta
            }
        }

        _uiState.update {
            it.copy(
                autorizado = true,
                totalRegistros = registros.size,
                totalTransito = transito,
                totalVisitas = visitas,
                totalNoDeterminado = noDeterminado,
                porcentajeTransito = porcentaje,
                registrosPorEdad = porEdad
            )
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}