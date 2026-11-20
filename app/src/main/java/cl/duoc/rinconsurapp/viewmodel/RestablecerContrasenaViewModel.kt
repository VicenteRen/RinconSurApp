package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.RestablecerContrasenaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RestablecerContrasenaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()
    private val _uiState = MutableStateFlow(RestablecerContrasenaUiState())
    val uiState: StateFlow<RestablecerContrasenaUiState> = _uiState.asStateFlow()

    fun onRutCambiado(nuevoRut: String) {
        if (nuevoRut.length > 9) return
        if (!nuevoRut.matches(Regex("^[0-9]*[kK]?$"))) return

        val error = if (nuevoRut.isNotBlank() && !validarRutModulo11(nuevoRut)) {
            "RUT ingresado inválido"
        } else null

        _uiState.update { estado ->
            val esValido = error == null && nuevoRut.isNotBlank() &&
                    estado.respuestaSeguridad.isNotBlank() &&
                    estado.tieneLongitudValida && estado.tieneNumero &&
                    estado.tieneMayuscula &&
                    estado.nuevaContrasena == estado.confirmarContrasena

            estado.copy(
                rut = nuevoRut,
                rutError = error,
                errorGeneral = null,
                formularioValido = esValido
            )
        }
    }

    fun onPreguntaSeleccionada(pregunta: String) {
        _uiState.update { it.copy(preguntaSeleccionada = pregunta) }
    }

    fun onRespuestaCambiada(nuevaRespuesta: String) {
        _uiState.update { estado ->
            val esValido = estado.rutError == null && estado.rut.isNotBlank() &&
                    nuevaRespuesta.isNotBlank() &&
                    estado.tieneLongitudValida && estado.tieneNumero &&
                    estado.tieneMayuscula &&
                    estado.nuevaContrasena == estado.confirmarContrasena

            estado.copy(
                respuestaSeguridad = nuevaRespuesta,
                respuestaError = null,
                errorGeneral = null,
                formularioValido = esValido
            )
        }
    }

    fun onNuevaContrasenaCambiada(contrasena: String) {
        _uiState.update { estado ->
            val longitudValida = contrasena.length >= 8
            val tieneNum = contrasena.any { it.isDigit() }
            val tieneMayusc = contrasena.any { it.isUpperCase() }

            val contrasenasCoinciden = contrasena == estado.confirmarContrasena
            val error = if (estado.confirmarContrasena.isNotBlank() && !contrasenasCoinciden) {
                "Las contraseñas no coinciden"
            } else null

            val esValido = estado.rutError == null && estado.rut.isNotBlank() &&
                    estado.respuestaSeguridad.isNotBlank() &&
                    longitudValida && tieneNum && tieneMayusc &&
                    contrasenasCoinciden

            estado.copy(
                nuevaContrasena = contrasena,
                tieneLongitudValida = longitudValida,
                tieneNumero = tieneNum,
                tieneMayuscula = tieneMayusc,
                contrasenaError = error,
                errorGeneral = null,
                formularioValido = esValido
            )
        }
    }

    fun onConfirmarContrasenaCambiada(contrasena: String) {
        _uiState.update { estado ->
            val contrasenasCoinciden = estado.nuevaContrasena == contrasena
            val error = if (contrasena.isNotBlank() && !contrasenasCoinciden) {
                "Las contraseñas no coinciden"
            } else null

            val esValido = estado.rutError == null && estado.rut.isNotBlank() &&
                    estado.respuestaSeguridad.isNotBlank() &&
                    estado.tieneLongitudValida && estado.tieneNumero &&
                    estado.tieneMayuscula &&
                    contrasenasCoinciden

            estado.copy(
                confirmarContrasena = contrasena,
                contrasenaError = error,
                errorGeneral = null,
                formularioValido = esValido
            )
        }
    }

    fun alternarVisibilidadContrasenas() {
        _uiState.update { it.copy(contrasenasVisibles = !it.contrasenasVisibles) }
    }

    fun intentarRestablecerContrasena() {
        val estado = _uiState.value
        viewModelScope.launch {
            try {
                repositorio.verificarIdentidad(
                    rut = estado.rut.trim(),
                    preguntaSeguridad = estado.preguntaSeleccionada,
                    respuestaSeguridad = estado.respuestaSeguridad.trim()
                )
                repositorio.recuperarContrasena(
                    rut = estado.rut.trim(),
                    nuevaContrasena = estado.nuevaContrasena
                )
                _uiState.update { it.copy(restablecimientoExitoso = true, errorGeneral = null) }
            } catch (e: Exception) {
                val mensaje = if (e.message?.startsWith("Error de red") == true) {
                    e.message
                } else {
                    "Los datos de seguridad no coinciden con ningún usuario."
                }
                _uiState.update {
                    it.copy(
                        restablecimientoExitoso = false,
                        errorGeneral = mensaje
                    )
                }
            }
        }
    }

    fun onDialogoExitoCerrado() {
        _uiState.update { RestablecerContrasenaUiState() }
    }

    private fun validarRutModulo11(rutCompleto: String): Boolean {
        val rutLimpio = rutCompleto.replace(Regex("[^0-9kK]"), "").uppercase()
        if (rutLimpio.length < 8) return false

        val numeroRut = rutLimpio.dropLast(1)
        val dvIngresado = rutLimpio.takeLast(1)

        var suma = 0
        var multiplicador = 2
        for (i in numeroRut.reversed()) {
            suma += i.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = suma % 11
        val dvCalculado = (11 - resto).toString()

        val dvEsperado = when (dvCalculado) {
            "11" -> "0"
            "10" -> "K"
            else -> dvCalculado
        }

        return dvIngresado == dvEsperado
    }
}