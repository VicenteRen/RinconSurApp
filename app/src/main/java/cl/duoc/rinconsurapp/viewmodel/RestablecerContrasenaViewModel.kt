package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.RestablecerContrasenaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RestablecerContrasenaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()

    private val _uiState = MutableStateFlow(
        RestablecerContrasenaUiState()
    )
    val uiState = _uiState.asStateFlow()

    fun onRutCambiado(rut: String) {
        if (rut.length > 9 ||
            !rut.matches(Regex("^[0-9]*[kK]?$"))) return

        _uiState.update {
            it.copy(
                rut = rut,
                rutError = if (rut.isNotEmpty() &&
                    !validarRutModulo11(rut)
                ) "RUT inválido" else null,
                identidadVerificada = false,
                errorGeneral = null
            )
        }
    }

    fun onPreguntaSeleccionada(pregunta: String) {
        _uiState.update {
            it.copy(
                preguntaSeleccionada = pregunta,
                identidadVerificada = false,
                errorGeneral = null
            )
        }
    }

    fun onRespuestaCambiada(respuesta: String) {
        _uiState.update {
            it.copy(
                respuestaSeguridad = respuesta,
                identidadVerificada = false,
                errorGeneral = null
            )
        }
    }

    fun verificarIdentidad() {
        val estado = _uiState.value

        if (estado.verificando ||
            !validarRutModulo11(estado.rut) ||
            estado.respuestaSeguridad.isBlank()
        ) {
            _uiState.update {
                it.copy(errorGeneral = "Completa los datos correctamente.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(verificando = true, errorGeneral = null)
            }

            try {
                repositorio.verificarIdentidad(
                    rut = estado.rut,
                    preguntaSeguridad = estado.preguntaSeleccionada,
                    respuestaSeguridad = estado.respuestaSeguridad
                )

                _uiState.update {
                    it.copy(
                        identidadVerificada = true,
                        verificando = false,
                        errorGeneral = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        identidadVerificada = false,
                        verificando = false,
                        errorGeneral =
                            "RUT, pregunta o respuesta incorrectos. Verifica tus datos."
                    )
                }
            }
        }
    }

    fun onNuevaContrasenaCambiada(contrasena: String) {
        if (contrasena.length > 64) return

        _uiState.update {
            actualizarValidaciones(
                it.copy(
                    nuevaContrasena = contrasena,
                    errorGeneral = null
                )
            )
        }
    }

    fun onConfirmarContrasenaCambiada(contrasena: String) {
        if (contrasena.length > 64) return

        _uiState.update {
            actualizarValidaciones(
                it.copy(
                    confirmarContrasena = contrasena,
                    errorGeneral = null
                )
            )
        }
    }

    private fun actualizarValidaciones(
        estado: RestablecerContrasenaUiState
    ): RestablecerContrasenaUiState {
        val clave = estado.nuevaContrasena

        val longitud = clave.length in 8..64
        val numero = clave.any { it.isDigit() }
        val mayuscula = clave.any { it.isUpperCase() }
        val especial = clave.any {
            !it.isLetterOrDigit() && !it.isWhitespace()
        }
        val coinciden = clave.isNotEmpty() &&
                clave == estado.confirmarContrasena

        return estado.copy(
            tieneLongitudValida = longitud,
            tieneNumero = numero,
            tieneMayuscula = mayuscula,
            tieneEspecial = especial,
            contrasenaError = if (
                estado.confirmarContrasena.isNotEmpty() &&
                !coinciden
            ) "Las contraseñas no coinciden" else null,
            formularioValido = longitud && numero &&
                    mayuscula && especial && coinciden
        )
    }

    fun alternarVisibilidadContrasenas() {
        _uiState.update {
            it.copy(contrasenasVisibles = !it.contrasenasVisibles)
        }
    }

    fun volverAlPrimerPaso() {
        _uiState.update {
            it.copy(
                identidadVerificada = false,
                nuevaContrasena = "",
                confirmarContrasena = "",
                formularioValido = false,
                errorGeneral = null
            )
        }
    }

    fun intentarRestablecerContrasena() {
        val estado = _uiState.value

        if (!estado.identidadVerificada ||
            !estado.formularioValido ||
            estado.verificando
        ) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(verificando = true, errorGeneral = null)
            }

            try {
                repositorio.recuperarContrasena(
                    rut = estado.rut,
                    preguntaSeguridad = estado.preguntaSeleccionada,
                    respuestaSeguridad = estado.respuestaSeguridad,
                    nuevaContrasena = estado.nuevaContrasena
                )

                _uiState.update {
                    it.copy(
                        verificando = false,
                        restablecimientoExitoso = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        verificando = false,
                        errorGeneral =
                            "No se pudo restablecer la contraseña. Verifica tus datos e inténtalo nuevamente."
                    )
                }
            }
        }
    }

    fun onDialogoExitoCerrado() {
        _uiState.value = RestablecerContrasenaUiState()
    }

    private fun validarRutModulo11(rut: String): Boolean {
        val limpio = rut.uppercase()
        if (!limpio.matches(Regex("^[0-9]{7,8}[0-9K]$"))) {
            return false
        }

        val cuerpo = limpio.dropLast(1)
        var suma = 0
        var multiplicador = 2

        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador =
                if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resultado = 11 - suma % 11
        val dv = when (resultado) {
            11 -> "0"
            10 -> "K"
            else -> resultado.toString()
        }

        return limpio.last().toString() == dv
    }
}