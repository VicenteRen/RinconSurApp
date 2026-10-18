package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.PerfilUsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PerfilUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RinconSurDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(PerfilUsuarioUiState())
    val uiState: StateFlow<PerfilUsuarioUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarioDeSesion()
    }

    fun cargarUsuarioDeSesion() {
        val idGuardado = sesion.leer()
        // Si no hay sesión activa aún en pruebas, cargamos el ID 1L (Observador) por defecto
        val idBuscar = if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L
        val usuario = db.buscarUsuarioPorId(idBuscar)

        if (usuario != null) {
            _uiState.update {
                it.copy(
                    nombreCompleto = usuario.nombreCompleto,
                    rut = usuario.rut,
                    cargo = usuario.rolNombre, // Viene del INNER JOIN con la tabla roles
                    telefonoContacto = usuario.telefono,
                    correoContacto = usuario.correo,
                    esValido = true,
                    informacionModificadaExitosamente = false,
                    cerrarSesionExitoso = false
                )
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
            val valido = nuevoTelefono.length == 9 && error == null && estado.errorCorreo == null && estado.correoContacto.isNotBlank()
            estado.copy(
                telefonoContacto = nuevoTelefono,
                errorTelefono = error,
                esValido = valido,
                informacionModificadaExitosamente = false
            )
        }
    }

    fun alCambiarCorreo(nuevoCorreo: String) {
        val formatoValido = nuevoCorreo.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))

        val error = if (nuevoCorreo.isNotBlank() && !formatoValido) {
            "Formato de correo inválido"
        } else if (nuevoCorreo.isBlank()) {
            "El correo es obligatorio"
        } else null

        _uiState.update { estado ->
            val valido = error == null && estado.errorTelefono == null && estado.telefonoContacto.length == 9
            estado.copy(
                correoContacto = nuevoCorreo,
                errorCorreo = error,
                esValido = valido,
                informacionModificadaExitosamente = false
            )
        }
    }

    fun guardarCambios() {
        val estado = _uiState.value
        if (estado.esValido) {
            val idGuardado = sesion.leer()
            val idActualizar = if (idGuardado != SesionPreferencias.SIN_SESION) idGuardado else 1L
            // UPDATE real en SQLite
            db.actualizarContactoUsuario(idActualizar, estado.correoContacto.trim(), estado.telefonoContacto)
            _uiState.update { it.copy(informacionModificadaExitosamente = true) }
        }
    }

    fun cerrarSesion() {
        sesion.borrar()
        _uiState.update { it.copy(cerrarSesionExitoso = true) }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}