package cl.duoc.rinconsurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
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

class RegistroObservacionViewModel : ViewModel() {

    private val repositorio = ObservacionRepository()
    private var usuarioIdActivo: Long = 1L

    private val _uiState = MutableStateFlow(RegistroObservacionUiState())
    val uiState: StateFlow<RegistroObservacionUiState> = _uiState.asStateFlow()

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

    fun cargarNombreObservador(context: Context) {
        val sesion = SesionPreferencias(context)
        val db = RinconSurDbHelper(context)
        try {
            val idGuardado = sesion.leer()
            usuarioIdActivo = if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L
            val usuario = db.buscarUsuarioPorId(usuarioIdActivo)
            if (usuario != null) {
                _uiState.update { it.copy(nombreObservador = usuario.nombreCompleto) }
            }
        } finally {
            db.close()
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

        val nuevoRegistro = RegistroObservacion(
            clasificacion = estadoActual.clasificacion.etiqueta,
            rangoEtario = estadoActual.rangoEtario.etiqueta,
            compania = estadoActual.compania.etiqueta,
            tieneComprasExternas = estadoActual.tieneComprasExternas,
            accesoDestino = estadoActual.accesoDestino?.etiqueta,
            usuarioId = usuarioIdActivo,
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
}