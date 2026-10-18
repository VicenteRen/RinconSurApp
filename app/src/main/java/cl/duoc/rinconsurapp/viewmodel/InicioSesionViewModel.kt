package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.InicioSesionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InicioSesionViewModel(application: Application) : AndroidViewModel(application) {

    // Conexión a SQLite y SharedPreferences
    private val db = RinconSurDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(InicioSesionUiState())
    val uiState: StateFlow<InicioSesionUiState> = _uiState.asStateFlow()

    fun onRutCambiado(nuevoRut: String) {
        if (nuevoRut.length > 9) return
        if (!nuevoRut.matches(Regex("^[0-9]*[kK]?$"))) return

        val error = if (nuevoRut.isNotBlank() && !validarRutModulo11(nuevoRut)) {
            "RUT ingresado inválido (ej: 123456789)"
        } else null

        _uiState.update { estado ->
            val valido = nuevoRut.isNotBlank() && error == null && estado.contrasenaError == null && estado.contrasena.isNotBlank()
            estado.copy(
                rut = nuevoRut,
                rutError = error,
                errorGeneral = null,
                esValido = valido
            )
        }
    }

    fun onContrasenaCambiada(nuevaContrasena: String) {
        val error = when {
            nuevaContrasena.isBlank() -> "Contraseña obligatoria"
            nuevaContrasena.length < 6 -> "Ingrese mínimo 6 caracteres"
            else -> null
        }

        _uiState.update { estado ->
            val valido = estado.rut.isNotBlank() && estado.rutError == null && nuevaContrasena.isNotBlank() && error == null
            estado.copy(
                contrasena = nuevaContrasena,
                contrasenaError = error,
                errorGeneral = null,
                esValido = valido
            )
        }
    }

    fun alternarVisibilidadContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        // Consulta real a SQLite con INNER JOIN a la tabla roles
        val usuarioEncontrado = db.autenticar(estado.rut.trim(), estado.contrasena)

        if (usuarioEncontrado != null) {
            // Guardamos en SharedPreferences quién inició sesión
            sesion.guardar(usuarioEncontrado.id)
            _uiState.update { it.copy(autenticado = true, errorGeneral = null) }
        } else {
            _uiState.update {
                it.copy(
                    autenticado = false,
                    errorGeneral = "RUT o contraseña incorrecta. Vuelva a intentarlo."
                )
            }
        }
    }

    private fun validarRutModulo11(rutCompleto: String): Boolean {
        val regex = Regex("^[0-9]{7,8}[0-9kK]{1}$")
        if (!regex.matches(rutCompleto.trim())) return false

        val rutLimpio = rutCompleto.trim()
        val numeroRut = rutLimpio.dropLast(1)
        val dvIngresado = rutLimpio.takeLast(1).uppercase()

        var suma = 0
        var multiplicador = 2
        for (i in numeroRut.reversed()) {
            suma += i.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = suma % 11
        val dvCalculado = (11 - resto).toString() // <-- CORRECCIÓN APLICADA AQUÍ

        val dvEsperado = when (dvCalculado) {
            "11" -> "0"
            "10" -> "K"
            else -> dvCalculado
        }

        return dvIngresado == dvEsperado
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}