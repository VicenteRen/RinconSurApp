package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.model.CrearUsuarioUiState
import cl.duoc.rinconsurapp.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CrearUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RinconSurDbHelper(application)
    private val _uiState = MutableStateFlow(CrearUsuarioUiState())
    val uiState: StateFlow<CrearUsuarioUiState> = _uiState.asStateFlow()

    fun inicializarListas(roles: List<String>, preguntas: List<String>) {
        if (_uiState.value.listaRoles.isEmpty()) {
            _uiState.update {
                it.copy(
                    listaRoles = roles,
                    listaPreguntas = preguntas,
                    rolSeleccionado = roles.firstOrNull() ?: "",
                    preguntaSeleccionada = preguntas.firstOrNull() ?: ""
                )
            }
        }
    }

    fun onNombreCambiado(nombre: String) {
        val nombreFiltrado = nombre.filter { it.isLetter() || it.isWhitespace() }
        val nombreFormateado = nombreFiltrado.split(" ").joinToString(" ") { palabra ->
            if (palabra.isNotEmpty()) palabra.lowercase().replaceFirstChar { it.uppercase() } else ""
        }
        _uiState.update { it.copy(nombre = nombreFormateado) }
        validarFormulario()
    }

    fun onCorreoCambiado(correo: String) {
        val correoMinuscula = correo.lowercase()
        val error = if (correoMinuscula.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(correoMinuscula).matches()) {
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
            } else {
                val rutDuplicado = db.listarUsuariosActivos().any { it.rut == nuevoRut }

                if (rutDuplicado) {
                    error = "Este RUT ya está en uso"
                }
            }
        }

        _uiState.update { it.copy(rut = nuevoRut, rutError = error) }
        validarFormulario()
    }

    fun onRolSeleccionado(rol: String) {
        _uiState.update { it.copy(rolSeleccionado = rol) }
        validarFormulario()
    }

    fun onPreguntaSeleccionada(pregunta: String) {
        _uiState.update { it.copy(preguntaSeleccionada = pregunta) }
        validarFormulario()
    }

    fun onRespuestaCambiada(respuesta: String) {
        if (!respuesta.all { it.isLetter() || it.isWhitespace() }) return
        _uiState.update { it.copy(respuestaSeguridad = respuesta) }
        validarFormulario()
    }

    fun onContrasenaCambiada(contrasena: String) {
        _uiState.update { estado ->
            val contrasenasCoinciden = contrasena == estado.confirmarContrasena
            val error = if (estado.confirmarContrasena.isNotBlank() && !contrasenasCoinciden) {
                "Las contraseñas no coinciden"
            } else null

            estado.copy(
                contrasena = contrasena,
                tieneLongitudValida = contrasena.length >= 8,
                tieneNumero = contrasena.any { c -> c.isDigit() },
                tieneMayuscula = contrasena.any { c -> c.isUpperCase() },
                sinCaracteresEspeciales = contrasena.isNotEmpty() && contrasena.all { it.isLetterOrDigit() },
                contrasenaError = error
            )
        }
        validarFormulario()
    }

    fun onConfirmarContrasenaCambiada(contrasena: String) {
        _uiState.update { estado ->
            val contrasenasCoinciden = estado.contrasena == contrasena
            val error = if (contrasena.isNotBlank() && !contrasenasCoinciden) {
                "Las contraseñas no coinciden"
            } else null

            estado.copy(confirmarContrasena = contrasena, contrasenaError = error)
        }
        validarFormulario()
    }

    fun alternarVisibilidadContrasenas() {
        _uiState.update { it.copy(contrasenasVisibles = !it.contrasenasVisibles) }
    }

    private fun validarFormulario() {
        _uiState.update { estado ->
            val esValido = estado.nombre.isNotBlank() &&
                    estado.rutError == null && estado.rut.isNotBlank() &&
                    estado.correoError == null && estado.correo.isNotBlank() &&
                    estado.telefono.length == 9 &&
                    estado.respuestaSeguridad.isNotBlank() &&
                    estado.tieneLongitudValida && estado.tieneNumero &&
                    estado.tieneMayuscula && estado.sinCaracteresEspeciales &&
                    (estado.contrasena == estado.confirmarContrasena)

            estado.copy(formularioValido = esValido)
        }
    }

    fun intentarCrearUsuario(textoRolAdmin: String) {
        val estado = _uiState.value
        val rolId = if (estado.rolSeleccionado == textoRolAdmin) Rol.ID_ADMIN else Rol.ID_OBSERVADOR

        try {
            val id = db.insertarUsuario(
                rut = estado.rut,
                contrasena = estado.contrasena,
                nombre = estado.nombre,
                correo = estado.correo,
                telefono = estado.telefono,
                preguntaSeguridad = estado.preguntaSeleccionada,
                respuestaSeguridad = estado.respuestaSeguridad,
                rolId = rolId
            )
            if (id > -1) {
                _uiState.update { it.copy(creacionExitosa = true, errorGeneral = null) }
            } else {
                _uiState.update { it.copy(errorGeneral = "El RUT ya existe y está activo en el sistema.") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorGeneral = "Error de base de datos.") }
        }
    }

    fun onDialogoExitoCerrado() {
        _uiState.update { it.copy(creacionExitosa = false) }
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

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}