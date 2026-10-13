package cl.duoc.rinconsurapp.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.rinconsurapp.model.PerfilUsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PerfilUsuarioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUsuarioUiState())
    val uiState: StateFlow<PerfilUsuarioUiState> = _uiState.asStateFlow()

    init {
        // SIMULACIÓN: Cuando abres la pantalla, fingimos que el Login nos pasó tu RUT
           // cargarDatosUsuario("214297353")
        // Para ver el perfil del Administrador en "cargo" comenta la línea de arriba y descomenta la de abajo:
         cargarDatosUsuario("228405566")
    }

    // Función que carga el Rol y los datos según quién inició sesión
    fun cargarDatosUsuario(rutIngresado: String) {
        if (rutIngresado == "214297353") {
            // Cargar datos de Observador
            _uiState.update {
                it.copy(
                    nombreCompleto = "Vicente Ponce",
                    rut = rutIngresado,
                    cargo = "Observador",
                    telefonoContacto = "987654321",
                    correoContacto = "v.ponce@rinconsur.cl"
                )
            }
        } else if (rutIngresado == "228405566") {
            // Cargar datos de Administrador
            _uiState.update {
                it.copy(
                    nombreCompleto = "Jefe Administrador",
                    rut = rutIngresado,
                    cargo = "Administrador",
                    telefonoContacto = "911223344",
                    correoContacto = "admin@rinconsur.cl"
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
        if (_uiState.value.esValido) {
            _uiState.update { it.copy(informacionModificadaExitosamente = true) }
        }
    }

    fun cerrarSesion() {
        _uiState.update { it.copy(cerrarSesionExitoso = true) }
    }
}