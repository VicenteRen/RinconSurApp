package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RestablecerContrasenaViewModelTest {
    private lateinit var viewModel: RestablecerContrasenaViewModel

    @Before
    fun preparar(){
        viewModel = RestablecerContrasenaViewModel(Application())
    }
    @Test
    fun onNuevaContrasenaCambiada_sinCaracterEspecial_noValidarFormulario(){
        val contrasena = "Clave123"
        viewModel.onNuevaContrasenaCambiada(contrasena)
        viewModel.onConfirmarContrasenaCambiada(contrasena)
        assertEquals(contrasena, viewModel.uiState.value.nuevaContrasena)
        assertTrue(viewModel.uiState.value.tieneLongitudValida)
        assertTrue(viewModel.uiState.value.tieneMayuscula)
        assertTrue(viewModel.uiState.value.tieneNumero)
        assertFalse(viewModel.uiState.value.tieneEspecial)
        assertFalse(viewModel.uiState.value.formularioValido)

    }

    @Test
    fun onConfirmarContrasenaCambiada_alCorregirCoincidencia_validaFormulario() {
        viewModel.onNuevaContrasenaCambiada("Clave123*")
        viewModel.onConfirmarContrasenaCambiada("Otra123*")

        viewModel.onConfirmarContrasenaCambiada("Clave123*")

        assertEquals("Clave123*", viewModel.uiState.value.nuevaContrasena)
        assertEquals("Clave123*", viewModel.uiState.value.confirmarContrasena)
        assertTrue(viewModel.uiState.value.tieneEspecial)
        assertTrue(viewModel.uiState.value.formularioValido)
        assertNull(viewModel.uiState.value.contrasenaError)
    }
}