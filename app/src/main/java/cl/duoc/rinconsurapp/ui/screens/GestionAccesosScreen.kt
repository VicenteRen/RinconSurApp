package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.Acceso
import cl.duoc.rinconsurapp.model.GestionAccesosUiState
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.TarjetaSeccion
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.viewmodel.GestionAccesosViewModel
import kotlinx.coroutines.launch

@Composable
fun GestionAccesosScreen(
    onAbrirMenu: () -> Unit = {},
    viewModel: GestionAccesosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun mostrarMensaje(texto: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(texto)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarAccesos(::mostrarMensaje)
    }

    GestionAccesosContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAbrirMenu = onAbrirMenu,
        onCodigoCambia = viewModel::onCodigoCambia,
        onNombreCambia = viewModel::onNombreCambia,
        onCrearAcceso = {
            viewModel.crearAcceso(
                onExito = { nuevo ->
                    val mensaje = if (nuevo.activo) {
                        "Acceso ${nuevo.etiqueta} creado y habilitado"
                    } else {
                        "Acceso ${nuevo.etiqueta} creado deshabilitado (ya hay 5 activos)"
                    }
                    mostrarMensaje(mensaje)
                },
                onError = ::mostrarMensaje
            )
        },
        onCambiarEstado = { acceso ->
            viewModel.cambiarEstado(
                acceso = acceso,
                onExito = { actualizado ->
                    val estadoTexto = if (actualizado.activo) "habilitado" else "deshabilitado"
                    mostrarMensaje("Acceso ${actualizado.codigo} $estadoTexto")
                },
                onError = ::mostrarMensaje
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionAccesosContent(
    uiState: GestionAccesosUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAbrirMenu: () -> Unit = {},
    onCodigoCambia: (String) -> Unit,
    onNombreCambia: (String) -> Unit,
    onCrearAcceso: () -> Unit,
    onCambiarEstado: (Acceso) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Accesos") },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Abrir menú"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primario,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(Alignment.Top)
                    .padding(8.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaSeccion(titulo = "Nuevo Punto de Acceso") {
                    OutlinedTextField(
                        value = uiState.codigo,
                        onValueChange = onCodigoCambia,
                        label = { Text("Código (ej: A5)") },
                        isError = uiState.errorCodigo != null,
                        supportingText = uiState.errorCodigo?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = onNombreCambia,
                        label = { Text("Nombre del acceso (ej: Acceso Boulevard)") },
                        isError = uiState.errorNombre != null,
                        supportingText = uiState.errorNombre?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    BotonPrincipal(
                        texto = "Crear Acceso",
                        onClick = onCrearAcceso,
                        habilitado = !uiState.cargando,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TarjetaSeccion(titulo = "Accesos Registrados (${uiState.listaAccesos.size})") {
                    if (uiState.listaAccesos.isEmpty() && !uiState.cargando) {
                        Text(
                            text = "No hay puntos de acceso registrados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    uiState.listaAccesos.forEach { acceso ->
                        val colorAcceso = when (acceso.codigo) {
                            "A1" -> Color(0xFF5E35B1)
                            "A2" -> Color(0xFF00897B)
                            "A3" -> Color(0xFFFB8C00)
                            "A4" -> Color(0xFFE53935)
                            else -> Primario
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (acceso.activo) colorAcceso.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (acceso.activo) colorAcceso else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = acceso.codigo,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (acceso.activo) Color.White else Color.DarkGray
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acceso.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (acceso.activo) "Habilitado para observadores" else "Deshabilitado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (acceso.activo) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                    )
                                }

                                Switch(
                                    checked = acceso.activo,
                                    onCheckedChange = { onCambiarEstado(acceso) },
                                    enabled = !uiState.cargando
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.cargando) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GestionAccesosPreview() {
    RinconSurAppTheme {
        GestionAccesosContent(
            uiState = GestionAccesosUiState(
                listaAccesos = listOf(
                    Acceso(1, "A1", "Acceso Norte", true),
                    Acceso(2, "A2", "Acceso Sur", true),
                    Acceso(3, "A3", "Estacionamiento", false)
                )
            ),
            onCodigoCambia = {},
            onNombreCambia = {},
            onCrearAcceso = {},
            onCambiarEstado = {}
        )
    }
}