package cl.duoc.rinconsurapp.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.rinconsurapp.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistroObservacionViewModel : ViewModel() {

    // Estado privado y mutable que solo el ViewModel puede modificar
    private val _uiState = MutableStateFlow(RegistroObservacionUiState())

    // Estado público y de solo lectura que la interfaz observará
    val uiState: StateFlow<RegistroObservacionUiState> = _uiState.asStateFlow()

    fun onClasificacionSeleccionada(clasificacion: Clasificacion) {
        _uiState.update {
            // RN-08: El destino solo importa si es Tránsito. Si es otra cosa, lo volvemos null.
            val destino = if (clasificacion == Clasificacion.TRANSITO) it.accesoDestino else null
            it.copy(clasificacion = clasificacion, accesoDestino = destino)
        }
    }

    fun onRangoEtarioSeleccionado(rango: RangoEtario) {
        _uiState.update { it.copy(rangoEtario = rango) }
    }

    fun onCompaniaSeleccionada(compania: Compania) {
        _uiState.update { it.copy(compania = compania) }
    }

    fun onTieneComprasCambia(tiene: Boolean) {
        _uiState.update { it.copy(tieneComprasExternas = tiene) }
    }

    fun onAccesoDestinoSeleccionado(acceso: Acceso) {
        _uiState.update { it.copy(accesoDestino = acceso) }
    }

    fun onRegistrar() {
        // A futuro, aquí conectaremos SQLite/Room para guardar los datos sin internet
        _uiState.update { it.copy(registroExitoso = true) }
    }

    fun onLimpiar() {
        // Resetea la pantalla a sus valores por defecto
        _uiState.value = RegistroObservacionUiState()
    }
}
