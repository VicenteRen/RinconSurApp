package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.ObservacionRepository
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroObservacionViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ObservacionRepository()
    private val db = RinconSurDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(RegistroObservacionUiState())
    val uiState: StateFlow<RegistroObservacionUiState> = _uiState.asStateFlow()

    init {
        cargarNombreObservador()
    }

    private fun ejecutar(onError: (String) -> Unit = {}, accion: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                accion()
            } catch (e: Exception) {
                onError(e.message ?: "Ocurrió un error inesperado")
            } finally {
                _uiState.update { it.copy(cargando = false) }
            }
        }
    }

    fun cargarNombreObservador() {
        val idGuardado = sesion.leer()
        val idUsuarioActivo = if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L
        val usuario = db.buscarUsuarioPorId(idUsuarioActivo)

        if (usuario != null) {
            _uiState.update { it.copy(nombreObservador = usuario.nombreCompleto) }
        }
    }

    fun cargarAccesos(onError: (String) -> Unit = {}) = ejecutar(onError) {
        val accesos = repositorio.listarAccesosActivos()
        _uiState.update { estado ->
            val seleccionActualSigueActiva = accesos.any { it.codigo == estado.accesoDestino?.codigo }
            estado.copy(
                listaAccesos = accesos,
                accesoDestino = if (seleccionActualSigueActiva) estado.accesoDestino else accesos.firstOrNull()
            )
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

    fun onRegistrar(onExito: () -> Unit, onError: (String) -> Unit) = ejecutar(onError) {
        val estadoActual = _uiState.value

        val idGuardado = sesion.leer()
        val idUsuarioActivo = if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L

        val nuevoRegistro = RegistroObservacion(
            clasificacion = estadoActual.clasificacion.etiqueta,
            rangoEtario = estadoActual.rangoEtario.etiqueta,
            compania = estadoActual.compania.etiqueta,
            tieneComprasExternas = estadoActual.tieneComprasExternas,
            accesoDestino = estadoActual.accesoDestino?.etiqueta,
            usuarioId = idUsuarioActivo,
            nombreObservador = estadoActual.nombreObservador
        )

        repositorio.guardarObservacion(nuevoRegistro)

        _uiState.update { estado ->
            RegistroObservacionUiState(
                nombreObservador = estado.nombreObservador,
                listaAccesos = estado.listaAccesos,
                accesoDestino = estado.accesoDestino,
                registroExitoso = true
            )
        }
        onExito()
    }

    fun onLimpiar() {
        _uiState.update { estado ->
            RegistroObservacionUiState(
                nombreObservador = estado.nombreObservador,
                listaAccesos = estado.listaAccesos,
                accesoDestino = estado.accesoDestino
            )
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}