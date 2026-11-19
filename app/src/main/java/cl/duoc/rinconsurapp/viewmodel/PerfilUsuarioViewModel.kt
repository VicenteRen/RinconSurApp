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

    private val repositorio = UsuarioRepository()
    private val sesion = SesionPreferencias(application)

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
                _uiState.update {
                    it.copy(
                        nombreCompleto = usuario.nombreCompleto,
                        rut = usuario.rut,
                        cargo = usuario.rolNombre,
                        telefonoContacto = usuario.telefono,
                        correoContacto = usuario.correo,
                        avatar = usuario.avatar,
                        errorCorreo = null,
                        errorTelefono = null,
                        esValido = true,
                        informacionModificadaExitosamente = false,
                        cerrarSesionExitoso = false
                    )
                }
            } catch (e: Exception) {
                // Manejo de error de red
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
                        avatar = usuarioActualizado.avatar ?: bytes,
                        informacionModificadaExitosamente = true
                    )
                }
            } catch (e: Exception) {
                // Manejo de error al subir avatar
            }
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
                    estado.correoContacto.isNotBlank()

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
        val formatoValido = correoMinuscula.matches(
            Regex("^[a-z0-9ñáéíóúü._%+-]+@[a-z0-9ñáéíóúü.-]+\\.[a-z]{2,6}$")
        )

        val error = if (correoMinuscula.isNotBlank() && !formatoValido) {
            "Formato de correo inválido"
        } else if (correoMinuscula.isBlank()) {
            "El correo es obligatorio"
        } else null

        _uiState.update { estado ->
            val errorTelefonoLimpio = if (estado.errorTelefono?.startsWith("El teléfono debe tener") == true) {
                estado.errorTelefono
            } else null

            val valido = error == null && errorTelefonoLimpio == null && estado.telefonoContacto.length == 9

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
        if (estado.esValido) {
            viewModelScope.launch {
                try {
                    val idActualizar = obtenerIdActivo()
                    repositorio.actualizarPerfil(
                        id = idActualizar,
                        nombreCompleto = estado.nombreCompleto,
                        correo = estado.correoContacto.trim(),
                        telefono = estado.telefonoContacto
                    )
                    _uiState.update {
                        it.copy(
                            errorCorreo = null,
                            errorTelefono = null,
                            informacionModificadaExitosamente = true
                        )
                    }
                } catch (e: Exception) {
                    val mensaje = e.message ?: "Error al actualizar la información"
                    _uiState.update { actual ->
                        if (mensaje.contains("teléfono", ignoreCase = true) ||
                            mensaje.contains("telefono", ignoreCase = true)
                        ) {
                            actual.copy(
                                errorTelefono = mensaje,
                                informacionModificadaExitosamente = false
                            )
                        } else {
                            actual.copy(
                                errorCorreo = mensaje,
                                informacionModificadaExitosamente = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun cerrarSesion() {
        sesion.borrar()
        _uiState.update { it.copy(cerrarSesionExitoso = true) }
    }
}