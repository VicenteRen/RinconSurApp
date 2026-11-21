package cl.duoc.rinconsurapp.viewmodel

import cl.duoc.rinconsurapp.model.Acceso
import cl.duoc.rinconsurapp.model.Clasificacion
import cl.duoc.rinconsurapp.model.Compania
import cl.duoc.rinconsurapp.model.RangoEtario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class RegistroObservacionViewModelTest {

    private lateinit var viewModel: RegistroObservacionViewModel

    @Before
    fun preparar() {
        viewModel = RegistroObservacionViewModel()
    }

    @Test
    fun estadoInicial_alCrearViewModel_tieneValoresPorDefecto() {
        assertEquals(Clasificacion.TRANSITO, viewModel.uiState.value.clasificacion)
        assertEquals(Compania.SOLO, viewModel.uiState.value.compania)
        assertEquals(RangoEtario.RANGO_26_40, viewModel.uiState.value.rangoEtario)
        assertFalse(viewModel.uiState.value.tieneComprasExternas)
    }

    @Test
    fun seleccionarOpciones_conDatosValidos_actualizaUiState() {
        val accesoSur = Acceso(id = 2, codigo = "A2", nombre = "Acceso Sur")

        viewModel.onAccesoDestinoSeleccionado(accesoSur)
        viewModel.onClasificacionSeleccionada(Clasificacion.VISITA)
        viewModel.onCompaniaSeleccionada(Compania.PAREJA)
        viewModel.onRangoEtarioSeleccionado(RangoEtario.RANGO_18_25)
        viewModel.onTieneComprasCambia(true)

        assertEquals("A2", viewModel.uiState.value.accesoDestino?.codigo)
        assertEquals(Clasificacion.VISITA, viewModel.uiState.value.clasificacion)
        assertEquals(Compania.PAREJA, viewModel.uiState.value.compania)
        assertEquals(RangoEtario.RANGO_18_25, viewModel.uiState.value.rangoEtario)
        assertTrue(viewModel.uiState.value.tieneComprasExternas)
    }

    @Test
    fun onLimpiar_despuesDeCambiarDatos_restauraFormularioManteniendoAcceso() {
        val accesoNorte = Acceso(id = 1, codigo = "A1", nombre = "Acceso Norte")
        viewModel.onAccesoDestinoSeleccionado(accesoNorte)
        viewModel.onClasificacionSeleccionada(Clasificacion.VISITA)
        viewModel.onCompaniaSeleccionada(Compania.GRUPO)
        viewModel.onTieneComprasCambia(true)

        viewModel.onLimpiar()

        assertEquals("A1", viewModel.uiState.value.accesoDestino?.codigo)
        assertEquals(Clasificacion.TRANSITO, viewModel.uiState.value.clasificacion)
        assertEquals(Compania.SOLO, viewModel.uiState.value.compania)
        assertFalse(viewModel.uiState.value.tieneComprasExternas)
    }

}