package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrearUsuarioViewModelTest {

    private lateinit var viewModel: CrearUsuarioViewModel

    @Before
    fun preparar() {
        viewModel = CrearUsuarioViewModel(Application())
        viewModel.inicializarRoles(listOf("Observador", "Administrador"))
    }

    private fun llenarFormularioValido() {
        viewModel.onNombreCambiado("ana maria")
        viewModel.onRutCambiado("214297353")
        viewModel.onCorreoCambiado("ANA@RINCONSUR.CL")
        viewModel.onTelefonoCambiado("912345678")
    }

    @Test
    fun formularioConDatosValidos_habilitaCreacionYNormalizaDatos() {
        llenarFormularioValido()

        assertTrue(viewModel.uiState.value.formularioValido)
        assertEquals("Ana Maria", viewModel.uiState.value.nombre)
        assertEquals("ana@rinconsur.cl", viewModel.uiState.value.correo)
        assertNull(viewModel.uiState.value.rutError)
        assertNull(viewModel.uiState.value.correoError)
    }

    @Test
    fun onRutCambiado_conRutInvalido_invalidaFormularioYMuestraError() {
        llenarFormularioValido()
        viewModel.onRutCambiado("123456789")

        assertFalse(viewModel.uiState.value.formularioValido)
        assertEquals("RUT inválido", viewModel.uiState.value.rutError)
    }

    @Test
    fun onCorreoCambiado_sinArroba_invalidaFormularioYMuestraError() {
        llenarFormularioValido()
        viewModel.onCorreoCambiado("anarinconsur.cl")

        assertFalse(viewModel.uiState.value.formularioValido)
        assertEquals("Correo inválido", viewModel.uiState.value.correoError)
    }

    @Test
    fun onTelefonoCambiado_conMenosDe9Digitos_invalidaFormulario() {
        llenarFormularioValido()
        viewModel.onTelefonoCambiado("91234")

        assertFalse(viewModel.uiState.value.formularioValido)
    }

    @Test
    fun onDialogoExitoCerrado_limpiaFormulario() {
        llenarFormularioValido()
        viewModel.onDialogoExitoCerrado()

        assertEquals("", viewModel.uiState.value.nombre)
        assertEquals("", viewModel.uiState.value.rut)
        assertEquals("", viewModel.uiState.value.correo)
        assertEquals("", viewModel.uiState.value.telefono)
        assertFalse(viewModel.uiState.value.formularioValido)
    }

    @Test
    fun onNombreCambiado_conNumerosYEspaciosExtra_filtraYFormateaCorrectamente() {
        viewModel.onNombreCambiado("   ana123   maría45!@#")

        assertEquals("Ana María", viewModel.uiState.value.nombre)
    }
}