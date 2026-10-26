package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroObservacionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RinconSurDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(RegistroObservacionUiState())
    val uiState: StateFlow<RegistroObservacionUiState> = _uiState.asStateFlow()

    init {
        cargarNombreObservador()
    }

    fun cargarNombreObservador() {
        val idGuardado = sesion.leer()
        val idUsuarioActivo = if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L
        val usuario = db.buscarUsuarioPorId(idUsuarioActivo)

        if (usuario != null) {
            _uiState.update { it.copy(nombreObservador = usuario.nombreCompleto) }
        }
    }

    fun onAccesoDestinoSeleccionado(acceso: Acceso) {
        _uiState.update { it.copy(accesoDestino = acceso, registroExitoso = false) }
    }

    fun onClasificacionSeleccionada(clasificacion: Clasificacion) {
        _uiState.update { it.copy(clasificacion = clasificacion, registroExitoso = false) }
    }

    fun onRangoEtarioSeleccionado(rango: RangoEtario) {
        _uiState.update { it.copy(rangoEtario = rango, registroExitoso = false) }
    }

    fun onCompaniaSeleccionada(compania: Compania) {
        _uiState.update { it.copy(compania = compania, registroExitoso = false) }
    }

    fun onTieneComprasCambia(tiene: Boolean) {
        _uiState.update { it.copy(tieneComprasExternas = tiene, registroExitoso = false) }
    }

    fun onRegistrar(): Boolean {
        val estadoActual = _uiState.value

        val idGuardado = sesion.leer()
        val idUsuarioActivo = if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L
        val fechaHoraActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val nuevoRegistro = RegistroObservacion(
            clasificacion = estadoActual.clasificacion.etiqueta,
            rangoEtario = estadoActual.rangoEtario.etiqueta,
            compania = estadoActual.compania.etiqueta,
            tieneComprasExternas = estadoActual.tieneComprasExternas,
            accesoDestino = estadoActual.accesoDestino.etiqueta,
            fechaHora = fechaHoraActual,
            usuarioId = idUsuarioActivo
        )

        val idRegistro = db.insertarRegistro(nuevoRegistro)

        if (idRegistro == -1L) {
            return false
        }

        _uiState.value = RegistroObservacionUiState(
            nombreObservador = estadoActual.nombreObservador,
            accesoDestino = estadoActual.accesoDestino,
            registroExitoso = true
        )
        return true
    }

    fun onLimpiar() {
        val estadoActual = _uiState.value
        _uiState.value = RegistroObservacionUiState(
            nombreObservador = estadoActual.nombreObservador,
            accesoDestino = estadoActual.accesoDestino
        )
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}