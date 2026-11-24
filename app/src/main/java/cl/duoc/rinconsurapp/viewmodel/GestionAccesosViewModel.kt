package cl.duoc.rinconsurapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.ObservacionRepository
import cl.duoc.rinconsurapp.model.Acceso
import cl.duoc.rinconsurapp.model.GestionAccesosUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GestionAccesosViewModel : ViewModel() {

    private val repositorio = ObservacionRepository()

    private val _uiState = MutableStateFlow(GestionAccesosUiState())
    val uiState: StateFlow<GestionAccesosUiState> = _uiState.asStateFlow()

    private fun ejecutar(onError: (String) -> Unit, accion: suspend () -> Unit) {
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

    fun cargarAccesos(onError: (String) -> Unit) = ejecutar(onError) {
        val accesos = repositorio.listarTodosLosAccesos()
        _uiState.update { it.copy(listaAccesos = accesos) }
    }

    fun onCodigoCambia(valor: String) {
        val limpio = valor.uppercase().filter { !it.isWhitespace() }.take(2)
        _uiState.update { it.copy(codigo = limpio, errorCodigo = null) }
    }

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun validar(): Boolean {
        val estado = _uiState.value
        val codigoLimpio = estado.codigo.trim().uppercase()
        val nombreLimpio = estado.nombre.trim()

        val formatoCodigoValido = Regex("^[A-ZÑ]{1}[0-9]{1}$")

        val errorCodigo = when {
            codigoLimpio.isBlank() -> "El código es obligatorio (ej: A5)"
            !codigoLimpio.matches(formatoCodigoValido) -> "Usa 1 letra (A-Z) y 1 número (0-9), ej: A5 o B1"
            else -> null
        }
        val errorNombre = if (nombreLimpio.isBlank()) "El nombre del acceso es obligatorio" else null

        _uiState.update { it.copy(errorCodigo = errorCodigo, errorNombre = errorNombre) }

        return errorCodigo == null && errorNombre == null
    }

    fun crearAcceso(onExito: (Acceso) -> Unit, onError: (String) -> Unit) {
        if (!validar()) return

        val codigoLimpio = _uiState.value.codigo.trim().uppercase()
        val nombreLimpio = _uiState.value.nombre.trim()

        ejecutar(onError) {
            val nuevo = repositorio.crearAcceso(codigoLimpio, nombreLimpio)
            val listaActualizada = (_uiState.value.listaAccesos + nuevo).sortedBy { it.codigo }
            _uiState.update {
                it.copy(
                    codigo = "",
                    nombre = "",
                    listaAccesos = listaActualizada
                )
            }
            onExito(nuevo)
        }
    }

    fun cambiarEstado(acceso: Acceso, onExito: (Acceso) -> Unit, onError: (String) -> Unit) = ejecutar(onError) {
        val actualizado = repositorio.cambiarEstadoAcceso(acceso.id)
        _uiState.update { estado ->
            estado.copy(
                listaAccesos = estado.listaAccesos.map {
                    if (it.id == actualizado.id) actualizado else it
                }
            )
        }
        onExito(actualizado)
    }
}