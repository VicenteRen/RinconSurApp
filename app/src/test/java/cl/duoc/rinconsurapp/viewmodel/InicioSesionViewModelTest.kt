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

}