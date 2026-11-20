package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.PrimerIngresoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrimerIngresoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(PrimerIngresoUiState())
    val uiState: StateFlow<PrimerIngresoUiState> = _uiState.asStateFlow()

    fun inicializarPreguntas(preguntas: List<String>) {
        if (_uiState.value.listaPreguntas.isEmpty()) {
            _uiState.update {
                it.copy(
                    listaPreguntas = preguntas,
                    preguntaSeleccionada = preguntas.firstOrNull() ?: ""
                )
            }
        }
    }

    fun limpiarFormulario() {
        _uiState.update { estado ->
            PrimerIngresoUiState(
                listaPreguntas = estado.listaPreguntas,
                preguntaSeleccionada = estado.listaPreguntas.firstOrNull() ?: ""
            )
        }
    }

    fun onPreguntaSeleccionada(pregunta: String) {
        _uiState.update { it.copy(preguntaSeleccionada = pregunta) }
        validarPaso1()
    }

    fun onRespuestaCambiada(respuesta: String) {
        val error = if (respuesta.isNotBlank() && respuesta.trim().length < 2) {
            "La respuesta es demasiado corta"
        } else null
        _uiState.update { it.copy(respuestaSeguridad = respuesta, respuestaError = error) }
        validarPaso1()
    }

    private fun validarPaso1() {
        _uiState.update { estado ->
            val valido = estado.preguntaSeleccionada.isNotBlank() &&
                    estado.respuestaSeguridad.trim().length >= 2 &&
                    estado.respuestaError == null
            estado.copy(paso1Valido = valido)
        }
    }

    fun onContrasenaCambiada(contrasena: String) {
        _uiState.update { estado ->
            val coinciden = contrasena == estado.confirmarContrasena
            val error = if (estado.confirmarContrasena.isNotBlank() && !coinciden) {
                "Las contraseñas no coinciden"
            } else null

            estado.copy(
                contrasena = contrasena,
                tieneLongitudValida = contrasena.length >= 8,
                tieneNumero = contrasena.any { it.isDigit() },
                tieneMayuscula = contrasena.any { it.isUpperCase() },
                tieneEspecial = contrasena.any {
                    !it.isLetterOrDigit() && !it.isWhitespace()
                },
                contrasenaError = error,
                errorGeneral = null
            )
        }
        validarPaso2()
    }

    fun onConfirmarContrasenaCambiada(contrasena: String) {
        _uiState.update { estado ->
            val coinciden = estado.contrasena == contrasena
            val error = if (contrasena.isNotBlank() && !coinciden) {
                "Las contraseñas no coinciden"
            } else null

            estado.copy(
                confirmarContrasena = contrasena,
                contrasenaError = error,
                errorGeneral = null
            )
        }
        validarPaso2()
    }

    fun alternarVisibilidadContrasenas() {
        _uiState.update { it.copy(contrasenasVisibles = !it.contrasenasVisibles) }
    }

    private fun validarPaso2() {
        _uiState.update { estado ->
            val valido = estado.tieneLongitudValida &&
                    estado.tieneNumero &&
                    estado.tieneMayuscula &&
                    estado.tieneEspecial &&
                    estado.contrasena.isNotBlank() &&
                    estado.contrasena == estado.confirmarContrasena
            estado.copy(paso2Valido = valido)
        }
    }

    fun finalizarPrimerIngreso(onCompletado: () -> Unit) {
        val estado = _uiState.value
        val idUsuario = sesion.leer()
        if (!estado.paso1Valido || !estado.paso2Valido || idUsuario == SesionPreferencias.SIN_SESION) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            try {
                repositorio.configurarPrimerIngreso(
                    id = idUsuario,
                    preguntaSeguridad = estado.preguntaSeleccionada,
                    respuestaSeguridad = estado.respuestaSeguridad,
                    nuevaContrasena = estado.contrasena
                )
                limpiarFormulario()
                onCompletado()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = e.message ?: "Error al guardar la configuración inicial"
                    )
                }
            }
        }
    }
}