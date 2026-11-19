package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoTexto
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.model.PrimerIngresoUiState
import cl.duoc.rinconsurapp.viewmodel.PrimerIngresoViewModel

@Composable
fun ConfigurarSeguridadScreen(
    viewModel: PrimerIngresoViewModel,
    onContinuar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val opcionesPreguntas = listOf(
        stringResource(R.string.preg_mascota),
        stringResource(R.string.preg_amigo),
        stringResource(R.string.preg_ciudad)
    )

    LaunchedEffect(Unit) {
        viewModel.inicializarPreguntas(opcionesPreguntas)
    }

    ConfigurarSeguridadContent(
        uiState = uiState,
        onPreguntaSeleccionada = { viewModel.onPreguntaSeleccionada(it) },
        onRespuestaCambiada = { viewModel.onRespuestaCambiada(it) },
        onContinuar = onContinuar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarSeguridadContent(
    uiState: PrimerIngresoUiState,
    onPreguntaSeleccionada: (String) -> Unit,
    onRespuestaCambiada: (String) -> Unit,
    onContinuar: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Primer Ingreso (1/2)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primario,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configura tu pregunta de seguridad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Esta pregunta te permitirá recuperar tu cuenta en caso de olvidar tu contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SelectorDesplegable(
                etiqueta = stringResource(R.string.etiqueta_seleccione_pregunta),
                opciones = uiState.listaPreguntas,
                seleccionActual = uiState.preguntaSeleccionada,
                onOpcionSeleccionada = onPreguntaSeleccionada
            )

            CampoTexto(
                valor = uiState.respuestaSeguridad,
                onValorCambia = onRespuestaCambiada,
                etiqueta = stringResource(R.string.etiqueta_respuesta),
                icono = R.drawable.ic_candado,
                error = uiState.respuestaError
            )

            BotonPrincipal(
                texto = "Continuar",
                onClick = onContinuar,
                habilitado = uiState.paso1Valido,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfigurarSeguridadPreview() {
    RinconSurAppTheme {
        ConfigurarSeguridadContent(
            uiState = PrimerIngresoUiState(
                listaPreguntas = listOf("¿Cuál es el nombre de tu primera mascota?"),
                preguntaSeleccionada = "¿Cuál es el nombre de tu primera mascota?"
            ),
            onPreguntaSeleccionada = {},
            onRespuestaCambiada = {},
            onContinuar = {}
        )
    }
}