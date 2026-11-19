package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.CrearUsuarioUiState
import cl.duoc.rinconsurapp.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CrearUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val CONTRASENA_TEMPORAL = "Temp1234*"
        private const val CARACTERES_ESPANOL = "ñÑáéíóúÁÉÍÓÚüÜ"
    }

    private val repositorio = UsuarioRepository()
    private val _uiState = MutableStateFlow(CrearUsuarioUiState())
    val uiState: StateFlow<CrearUsuarioUiState> = _uiState.asStateFlow()

    fun inicializarRoles(roles: List<String>) {
        if (_uiState.value.listaRoles.isEmpty()) {
            _uiState.update {
                it.copy(
                    listaRoles = roles,
                    rolSeleccionado = roles.firstOrNull() ?: ""
                )
            }
        }
    }

    fun onNombreCambiado(nombre: String) {
        val nombreFiltrado = nombre.filter { it.isLetter() || it.isWhitespace() || it in CARACTERES_ESPANOL }
        val nombreFormateado = nombreFiltrado.split(" ").joinToString(" ") { palabra ->
            if (palabra.isNotEmpty()) palabra.lowercase().replaceFirstChar { it.uppercase() } else ""
        }
        _uiState.update { it.copy(nombre = nombreFormateado) }
        validarFormulario()
    }

    fun onCorreoCambiado(correo: String) {
        val correoMinuscula = correo.lowercase()
        val patronCorreo = Regex("^[a-z0-9ñáéíóúü._%+-]+@[a-z0-9ñáéíóúü.-]+\\.[a-z]{2,6}$")

        val error = if (correoMinuscula.isNotBlank() && !correoMinuscula.matches(patronCorreo)) {
            "Correo inválido"
        } else null

        _uiState.update { it.copy(correo = correoMinuscula, correoError = error) }
        validarFormulario()
    }

    fun onTelefonoCambiado(telefono: String) {
        val telefonoLimpio = telefono.filter { it.isDigit() }
        if (telefonoLimpio.length > 9) return

        _uiState.update { it.copy(telefono = telefonoLimpio) }
        validarFormulario()
    }

    fun onRutCambiado(nuevoRut: String) {
        if (nuevoRut.length > 9) return
        if (!nuevoRut.matches(Regex("^[0-9]*[kK]?$"))) return

        var error: String? = null
        if (nuevoRut.isNotBlank()) {
            if (!validarRutModulo11(nuevoRut)) {
                error = "RUT inválido"
            }
        }

        _uiState.update { it.copy(rut = nuevoRut, rutError = error) }
        validarFormulario()
    }

    fun onRolSeleccionado(rol: String) {
        _uiState.update { it.copy(rolSeleccionado = rol) }
        validarFormulario()
    }

    private fun validarFormulario() {
        _uiState.update { estado ->
            val esValido = estado.nombre.isNotBlank() &&
                    estado.rutError == null && estado.rut.isNotBlank() &&
                    estado.correoError == null && estado.correo.isNotBlank() &&
                    estado.telefono.length == 9

            estado.copy(formularioValido = esValido)
        }
    }

    fun intentarCrearUsuario(textoRolAdmin: String) {
        val estado = _uiState.value
        val rolId = if (estado.rolSeleccionado == textoRolAdmin) Rol.ID_ADMIN else Rol.ID_OBSERVADOR

        viewModelScope.launch {
            try {
                repositorio.crearUsuario(
                    rut = estado.rut,
                    contrasena = CONTRASENA_TEMPORAL,
                    nombreCompleto = estado.nombre,
                    correo = estado.correo,
                    telefono = estado.telefono,
                    preguntaSeguridad = "Pregunta inicial",
                    respuestaSeguridad = "Respuesta inicial",
                    rolId = rolId
                )
                _uiState.update { it.copy(creacionExitosa = true, errorGeneral = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = e.message ?: "El RUT ya existe o error en el servidor.") }
            }
        }
    }

    fun onDialogoExitoCerrado() {
        _uiState.update {
            it.copy(
                creacionExitosa = false,
                nombre = "",
                rut = "",
                correo = "",
                telefono = "",
                formularioValido = false
            )
        }
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
        val dvCalculado = (11 - (suma % 11)).toString()
        val dvEsperado = when (dvCalculado) {
            "11" -> "0"
            "10" -> "K"
            else -> dvCalculado
        }
        return dvIngresado == dvEsperado
    }
}