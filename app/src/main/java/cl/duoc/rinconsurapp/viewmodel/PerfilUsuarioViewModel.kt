package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsurapp.data.ConvertidorImagen
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.PerfilUsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val PATRON_CORREO = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")
    }

    private val repositorio = UsuarioRepository()
    private val sesion = SesionPreferencias(application)

    private var perfilGuardado: Triple<String, String, String>? = null
    private val _uiState = MutableStateFlow(PerfilUsuarioUiState())
    val uiState: StateFlow<PerfilUsuarioUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarioDeSesion()
    }

    private fun obtenerIdActivo(): Long {
        val idGuardado = sesion.leer()
        return if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L
    }

    fun cargarUsuarioDeSesion() {
        viewModelScope.launch {
            try {
                val usuario = repositorio.buscarPorId(obtenerIdActivo())

                perfilGuardado = Triple(
                    usuario.nombreCompleto,
                    usuario.correo,
                    usuario.telefono
                )

                _uiState.update {
                    it.copy(
                        nombreCompleto = usuario.nombreCompleto,
                        rut = usuario.rut,
                        cargo = usuario.rolNombre,
                        telefonoContacto = usuario.telefono,
                        correoContacto = usuario.correo,
                        avatarUrl = usuario.avatarUrl,
                        errorNombre = null,
                        errorCorreo = null,
                        errorTelefono = null,
                        esValido = true,
                        informacionModificadaExitosamente = false,
                        cerrarSesionExitoso = false
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun cambiarAvatarDesdeCamara(foto: Bitmap) {
        guardarAvatar(ConvertidorImagen.aBytes(foto))
    }

    fun cambiarAvatarDesdeGaleria(uri: Uri) {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri) ?: return
        guardarAvatar(ConvertidorImagen.aBytes(imagen))
    }

    private fun guardarAvatar(bytes: ByteArray) {
        val idUsuario = obtenerIdActivo()
        viewModelScope.launch {
            try {
                val usuarioActualizado = repositorio.actualizarAvatar(idUsuario, bytes)
                _uiState.update {
                    it.copy(
                        avatarUrl = usuarioActualizado.avatarUrl,
                        informacionModificadaExitosamente = true,
                        mensajeActualizacion = "Foto de perfil actualizada correctamente."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        informacionModificadaExitosamente = false,
                        mensajeActualizacion = e.message ?: "No se pudo subir la foto de perfil."
                    )
                }
            }
        }
    }

    private fun nombreValido(nombre: String): Boolean {
        val limpio = nombre.trim().replace(Regex(" +"), " ")
        return limpio.length in 2..100 &&
                limpio.matches(Regex("^[\\p{L}]+(?: [\\p{L}]+)*$"))
    }

    private fun normalizarNombre(nombre: String): String {
        return nombre.trim()
            .replace(Regex(" +"), " ")
            .lowercase()
            .split(" ")
            .joinToString(" ") { palabra ->
                palabra.replaceFirstChar { it.titlecase() }
            }
    }

    fun alCambiarNombre(nuevoNombre: String) {
        if (nuevoNombre.length > 100) return
        if (!nuevoNombre.all { it.isLetter() || it == ' ' }) return

        val error = when {
            nuevoNombre.trim().isEmpty() ->
                "El nombre es obligatorio."

            !nombreValido(nuevoNombre) ->
                "Ingresa un nombre válido de 2 a 100 letras y espacios."

            else -> null
        }

        _uiState.update { estado ->
            estado.copy(
                nombreCompleto = nuevoNombre,
                errorNombre = error,
                esValido = error == null &&
                        estado.errorCorreo == null &&
                        estado.errorTelefono == null &&
                        estado.correoContacto.trim().matches(PATRON_CORREO) &&
                        estado.telefonoContacto.length == 9,
                informacionModificadaExitosamente = false
            )
        }
    }

    fun alCambiarTelefono(nuevoTelefono: String) {
        if (nuevoTelefono.length > 9) return
        if (!nuevoTelefono.matches(Regex("^[0-9]*$"))) return

        val error = if (nuevoTelefono.isNotBlank() && nuevoTelefono.length < 9) {
            "El teléfono debe tener 9 dígitos (ej: 912345678)"
        } else null

        _uiState.update { estado ->
            val errorCorreoLimpio = if (
                estado.errorCorreo == "Formato de correo inválido" ||
                estado.errorCorreo == "El correo es obligatorio"
            ) {
                estado.errorCorreo
            } else null

            val valido = nuevoTelefono.length == 9 &&
                    error == null &&
                    errorCorreoLimpio == null &&
                    estado.correoContacto.trim().matches(PATRON_CORREO) &&
                    nombreValido(estado.nombreCompleto) &&
                    estado.errorNombre == null

            estado.copy(
                telefonoContacto = nuevoTelefono,
                errorTelefono = error,
                errorCorreo = errorCorreoLimpio,
                esValido = valido,
                informacionModificadaExitosamente = false
            )
        }
    }

    fun alCambiarCorreo(nuevoCorreo: String) {
        val correoMinuscula = nuevoCorreo.lowercase()
        val correoLimpio = correoMinuscula.trim()
        val formatoValido = correoLimpio.matches(PATRON_CORREO)

        val error = if (correoLimpio.isNotBlank() && !formatoValido) {
            "Formato de correo inválido"
        } else if (correoLimpio.isBlank()) {
            "El correo es obligatorio"
        } else null

        _uiState.update { estado ->
            val errorTelefonoLimpio = if (estado.errorTelefono?.startsWith("El teléfono debe tener") == true) {
                estado.errorTelefono
            } else null

            val valido = error == null &&
                    errorTelefonoLimpio == null &&
                    estado.telefonoContacto.length == 9 &&
                    nombreValido(estado.nombreCompleto) &&
                    estado.errorNombre == null

            estado.copy(
                correoContacto = correoMinuscula,
                errorCorreo = error,
                errorTelefono = errorTelefonoLimpio,
                esValido = valido,
                informacionModificadaExitosamente = false
            )
        }
    }

    fun guardarCambios() {
        val estado = _uiState.value

        if (!estado.esValido || !nombreValido(estado.nombreCompleto)) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(mensajeActualizacion = null)
            }

            try {
                val idActualizar = obtenerIdActivo()
                val anteriores = perfilGuardado

                val usuarioActualizado = repositorio.actualizarPerfil(
                    id = idActualizar,
                    nombreCompleto = normalizarNombre(estado.nombreCompleto),
                    correo = estado.correoContacto.trim(),
                    telefono = estado.telefonoContacto
                )

                val nuevoNombre = usuarioActualizado.nombreCompleto
                val nuevoCorreo = usuarioActualizado.correo
                val nuevoTelefono = usuarioActualizado.telefono

                val cambioNombre = anteriores == null ||
                        anteriores.first != nuevoNombre

                val cambioCorreo = anteriores == null ||
                        anteriores.second != nuevoCorreo

                val cambioTelefono = anteriores == null ||
                        anteriores.third != nuevoTelefono

                val totalCambios = listOf(
                    cambioNombre,
                    cambioCorreo,
                    cambioTelefono
                ).count { it }

                val mensaje = when {
                    totalCambios == 0 ->
                        "No se detectaron cambios en el perfil."

                    totalCambios > 1 ->
                        "Datos del perfil actualizados correctamente."

                    cambioNombre ->
                        "Nombre actualizado a $nuevoNombre."

                    cambioCorreo ->
                        "Correo actualizado a $nuevoCorreo."

                    else ->
                        "Teléfono actualizado a $nuevoTelefono."
                }

                perfilGuardado = Triple(
                    nuevoNombre,
                    nuevoCorreo,
                    nuevoTelefono
                )

                _uiState.update {
                    it.copy(
                        nombreCompleto = nuevoNombre,
                        correoContacto = nuevoCorreo,
                        telefonoContacto = nuevoTelefono,
                        errorNombre = null,
                        errorCorreo = null,
                        errorTelefono = null,
                        esValido = true,
                        informacionModificadaExitosamente = true,
                        mensajeActualizacion = mensaje
                    )
                }
            } catch (e: Exception) {
                val mensaje = e.message
                    ?: "No se pudo actualizar el perfil."

                _uiState.update { actual ->
                    when {
                        mensaje.contains("nombre", ignoreCase = true) ->
                            actual.copy(
                                errorNombre = mensaje,
                                informacionModificadaExitosamente = false,
                                mensajeActualizacion = null
                            )

                        mensaje.contains("teléfono", ignoreCase = true) ||
                                mensaje.contains("telefono", ignoreCase = true) ->
                            actual.copy(
                                errorTelefono = mensaje,
                                informacionModificadaExitosamente = false,
                                mensajeActualizacion = null
                            )

                        else ->
                            actual.copy(
                                errorCorreo = mensaje,
                                informacionModificadaExitosamente = false,
                                mensajeActualizacion = null
                            )
                    }
                }
            }
        }
    }

    fun cambiarContrasena(
        actual: String,
        nueva: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        if (actual.isBlank()) {
            onResultado(false, "Ingresa tu contraseña actual.")
            return
        }

        val valida = nueva.length >= 8 &&
                nueva.any { it.isUpperCase() } &&
                nueva.any { it.isDigit() } &&
                nueva.any {
                    !it.isLetterOrDigit() && !it.isWhitespace()
                }

        if (!valida) {
            onResultado(
                false,
                "La nueva contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."
            )
            return
        }

        val idUsuario = sesion.leer()

        if (idUsuario == SesionPreferencias.SIN_SESION) {
            onResultado(false, "Debes iniciar sesión nuevamente.")
            return
        }

        viewModelScope.launch {
            try {
                repositorio.cambiarContrasenaDesdePerfil(
                    id = idUsuario,
                    contrasenaActual = actual,
                    nuevaContrasena = nueva
                )

                onResultado(
                    true,
                    "Contraseña actualizada correctamente."
                )
            } catch (e: Exception) {
                onResultado(
                    false,
                    e.message ?: "No se pudo cambiar la contraseña."
                )
            }
        }
    }

    fun limpiarMensajeActualizacion() {
        _uiState.update {
            it.copy(mensajeActualizacion = null)
        }
    }

    fun cerrarSesion() {
        sesion.borrar()
        _uiState.update { it.copy(cerrarSesionExitoso = true) }
    }
}