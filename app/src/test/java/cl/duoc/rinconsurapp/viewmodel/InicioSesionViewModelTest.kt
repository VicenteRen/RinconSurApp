package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InicioSesionViewModelTest {

    private lateinit var viewModel: InicioSesionViewModel

    @Before
    fun preparar() {
        viewModel = InicioSesionViewModel(Application())
    }

    @Test
    fun onRutCambiado_conRutInvalido_muestraErrorYDeshabilitaFormulario() {
        viewModel.onRutCambiado("214297359")
        viewModel.onContrasenaCambiada("Observador123*")

        assertFalse(viewModel.uiState.value.esValido)
        assertEquals("RUT ingresado inválido (ej: 123456789)", viewModel.uiState.value.rutError)
        assertNull(viewModel.uiState.value.contrasenaError)
    }

    @Test
    fun onDatosValidos_habilitaFormularioSinErrores() {
        viewModel.onRutCambiado("214297353")
        viewModel.onContrasenaCambiada("Observador123*")

        assertTrue(viewModel.uiState.value.esValido)
        assertNull(viewModel.uiState.value.rutError)
        assertNull(viewModel.uiState.value.contrasenaError)
    }

    @Test
    fun onContrasenaCambiada_cortaOConEspacios_muestraError() {
        viewModel.onRutCambiado("214297353")
        viewModel.onContrasenaCambiada("123")

        assertFalse(viewModel.uiState.value.esValido)
        assertEquals("Ingrese mínimo 6 caracteres", viewModel.uiState.value.contrasenaError)

        viewModel.onContrasenaCambiada("clave 123")

        assertFalse(viewModel.uiState.value.esValido)
        assertEquals("No pueden haber espacios en blanco", viewModel.uiState.value.contrasenaError)
    }

    @Test
    fun alternarVisibilidadContrasena_cambiaEstado() {
        assertFalse(viewModel.uiState.value.contrasenaVisible)

        viewModel.alternarVisibilidadContrasena()

        assertTrue(viewModel.uiState.value.contrasenaVisible)
    }
}