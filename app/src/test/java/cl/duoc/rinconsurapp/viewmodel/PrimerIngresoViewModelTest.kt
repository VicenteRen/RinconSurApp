package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrimerIngresoViewModelTest {

    private lateinit var viewModel: PrimerIngresoViewModel

    @Before
    fun preparar() {
        viewModel = PrimerIngresoViewModel(Application())
        viewModel.inicializarPreguntas(listOf("¿Cuál es el nombre de tu primera mascota?"))
    }

    @Test
    fun paso1_conRespuestaCorta_muestraErrorYNoValida() {
        viewModel.onRespuestaCambiada("a")

        assertFalse(viewModel.uiState.value.paso1Valido)
        assertEquals("La respuesta es demasiado corta", viewModel.uiState.value.respuestaError)
    }

    @Test
    fun paso1_conPreguntaYRespuestaValida_habilitaPaso1() {
        viewModel.onRespuestaCambiada("Princesa")

        assertTrue(viewModel.uiState.value.paso1Valido)
        assertNull(viewModel.uiState.value.respuestaError)
    }

    @Test
    fun paso2_conContrasenaFuerteYCoincidente_habilitaPaso2() {
        viewModel.onContrasenaCambiada("Clave123*")
        viewModel.onConfirmarContrasenaCambiada("Clave123*")

        assertTrue(viewModel.uiState.value.tieneLongitudValida)
        assertTrue(viewModel.uiState.value.tieneMayuscula)
        assertTrue(viewModel.uiState.value.tieneNumero)
        assertTrue(viewModel.uiState.value.tieneEspecial)
        assertTrue(viewModel.uiState.value.paso2Valido)
        assertNull(viewModel.uiState.value.contrasenaError)
    }

    @Test
    fun paso2_conContrasenasDistintas_muestraErrorYNoValida() {
        viewModel.onContrasenaCambiada("Clave123*")
        viewModel.onConfirmarContrasenaCambiada("Otra123*")

        assertFalse(viewModel.uiState.value.paso2Valido)
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.contrasenaError)
    }

    @Test
    fun alternarVisibilidadContrasenas_cambiaEstado() {
        assertFalse(viewModel.uiState.value.contrasenasVisibles)

        viewModel.alternarVisibilidadContrasenas()

        assertTrue(viewModel.uiState.value.contrasenasVisibles)
    }
}