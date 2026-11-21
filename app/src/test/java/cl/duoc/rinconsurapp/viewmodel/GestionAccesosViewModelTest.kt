package cl.duoc.rinconsurapp.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GestionAccesosViewModelTest {

    private lateinit var viewModel: GestionAccesosViewModel

    @Before
    fun preparar() {
        viewModel = GestionAccesosViewModel()
    }

    @Test
    fun validar_conCamposVacios_devuelveFalseConErrores() {
        val resultado = viewModel.validar()

        assertFalse(resultado)
        assertEquals("El código es obligatorio (ej: A5)", viewModel.uiState.value.errorCodigo)
        assertEquals("El nombre del acceso es obligatorio", viewModel.uiState.value.errorNombre)
    }

    @Test
    fun validar_conCodigoFormatoInvalido_devuelveFalseConErrorCodigo() {
        viewModel.onCodigoCambia("PEPITO100")
        viewModel.onNombreCambia("Acceso Norte")

        val resultado = viewModel.validar()

        assertFalse(resultado)
        assertEquals("PE", viewModel.uiState.value.codigo)
        assertEquals("Usa 1 letra (A-Z) y 1 número (0-9), ej: A5 o B1", viewModel.uiState.value.errorCodigo)
        assertNull(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun validar_conDatosCompletos_devuelveTrueSinErrores() {
        viewModel.onCodigoCambia("B5")
        viewModel.onNombreCambia("Acceso Boulevard")

        val resultado = viewModel.validar()

        assertTrue(resultado)
        assertEquals("B5", viewModel.uiState.value.codigo)
        assertEquals("Acceso Boulevard", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.errorCodigo)
        assertNull(viewModel.uiState.value.errorNombre)
    }

}