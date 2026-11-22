package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoContrasena
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.model.PrimerIngresoUiState
import cl.duoc.rinconsurapp.viewmodel.PrimerIngresoViewModel

@Composable
fun CrearContrasenaScreen(
    viewModel: PrimerIngresoViewModel,
    onPrimerIngresoCompletado: () -> Unit,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CrearContrasenaContent(
        uiState = uiState,
        onContrasenaCambiada = { viewModel.onContrasenaCambiada(it) },
        onConfirmarContrasenaCambiada = { viewModel.onConfirmarContrasenaCambiada(it) },
        onGuardar = { viewModel.finalizarPrimerIngreso(onPrimerIngresoCompletado) },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearContrasenaContent(
    uiState: PrimerIngresoUiState,
    onContrasenaCambiada: (String) -> Unit,
    onConfirmarContrasenaCambiada: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {

    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Primer Ingreso (2/2)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primario,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                text = "Crea tu contraseña segura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = onContrasenaCambiada,
                etiqueta = stringResource(R.string.etiqueta_nueva_contrasena),
                visible = mostrarContrasena,
                onAlternarVisible = { mostrarContrasena = !mostrarContrasena },
                error = null
            )

            CampoContrasena(
                valor = uiState.confirmarContrasena,
                onValorCambia = onConfirmarContrasenaCambiada,
                etiqueta = stringResource(R.string.etiqueta_confirmar_contrasena),
                visible = mostrarConfirmacion,
                onAlternarVisible = { mostrarConfirmacion = !mostrarConfirmacion },
                error = uiState.contrasenaError
            )

            Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                Text(
                    text = stringResource(R.string.titulo_requisitos_contrasena),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                RequisitoTexto(texto = stringResource(R.string.req_longitud), cumplido = uiState.tieneLongitudValida)
                RequisitoTexto(texto = stringResource(R.string.req_numero), cumplido = uiState.tieneNumero)
                RequisitoTexto(texto = stringResource(R.string.req_mayuscula), cumplido = uiState.tieneMayuscula)
                RequisitoTexto(
                    texto = "Al menos un carácter especial",
                    cumplido = uiState.tieneEspecial
                )
            }

            if (uiState.errorGeneral != null) {
                Text(
                    text = uiState.errorGeneral,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            BotonPrincipal(
                texto = if (uiState.cargando) "Guardando..." else "Guardar y Continuar",
                onClick = onGuardar,
                habilitado = uiState.paso2Valido && !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrearContrasenaPreview() {
    RinconSurAppTheme {
        CrearContrasenaContent(
            uiState = PrimerIngresoUiState(),
            onContrasenaCambiada = {},
            onConfirmarContrasenaCambiada = {},
            onGuardar = {},
            onVolver = {}
        )
    }
}