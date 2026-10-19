package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.*
import cl.duoc.rinconsurapp.ui.components.*
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.viewmodel.RegistroObservacionViewModel
import kotlinx.coroutines.launch

@Composable
fun RegistroObservacionScreen(
    onAbrirMenu: () -> Unit = {},
    viewModel: RegistroObservacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val mensajeExito = stringResource(R.string.msg_registro_exitoso)

    RegistroObservacionContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAbrirMenu = onAbrirMenu,
        onAccesoDestinoSeleccionado = viewModel::onAccesoDestinoSeleccionado,
        onClasificacionSeleccionada = viewModel::onClasificacionSeleccionada,
        onTieneComprasCambia = viewModel::onTieneComprasCambia,
        onCompaniaSeleccionada = viewModel::onCompaniaSeleccionada,
        onRangoEtarioSeleccionado = viewModel::onRangoEtarioSeleccionado,
        onLimpiar = viewModel::onLimpiar,
        onRegistrar = {
            viewModel.onRegistrar()
            scope.launch {
                snackbarHostState.showSnackbar(mensajeExito)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroObservacionContent(
    uiState: RegistroObservacionUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAbrirMenu: () -> Unit = {},
    onAccesoDestinoSeleccionado: (Acceso) -> Unit,
    onClasificacionSeleccionada: (Clasificacion) -> Unit,
    onTieneComprasCambia: (Boolean) -> Unit,
    onCompaniaSeleccionada: (Compania) -> Unit,
    onRangoEtarioSeleccionado: (RangoEtario) -> Unit,
    onLimpiar: () -> Unit,
    onRegistrar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_observador, uiState.nombreObservador)) },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_menu),
                            contentDescription = "Abrir menú"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 01. Punto de Acceso (Siempre visible para cualquier persona que entre)
            TarjetaSeccion(titulo = stringResource(R.string.seccion_acceso)) {
                GrupoRadio(
                    opciones = Acceso.entries,
                    seleccionada = uiState.accesoDestino,
                    onSeleccionar = onAccesoDestinoSeleccionado,
                    textoDe = { it.etiqueta }
                )
            }

            // 02. Clasificación
            TarjetaSeccion(titulo = stringResource(R.string.seccion_clasificacion)) {
                GrupoRadio(
                    opciones = Clasificacion.entries,
                    seleccionada = uiState.clasificacion,
                    onSeleccionar = onClasificacionSeleccionada,
                    textoDe = { it.etiqueta }
                )
            }

            // 03. Variables Adicionales (¿Trae compras?)
            TarjetaSeccion(titulo = stringResource(R.string.seccion_variables)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.pregunta_compras),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.tieneComprasExternas,
                        onCheckedChange = onTieneComprasCambia
                    )
                }
            }

            // 04. Compañía
            TarjetaSeccion(titulo = stringResource(R.string.seccion_compania)) {
                GrupoRadio(
                    opciones = Compania.entries,
                    seleccionada = uiState.compania,
                    onSeleccionar = onCompaniaSeleccionada,
                    textoDe = { it.etiqueta }
                )
            }

            // 05. Rango Etario
            TarjetaSeccion(titulo = stringResource(R.string.seccion_rango_etario)) {
                GrupoRadio(
                    opciones = RangoEtario.entries,
                    seleccionada = uiState.rangoEtario,
                    onSeleccionar = onRangoEtarioSeleccionado,
                    textoDe = { it.etiqueta }
                )
            }

            // Botones Limpiar y Guardar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_limpiar),
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_guardar),
                    onClick = onRegistrar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun RegistroObservacionPreview() {
    RinconSurAppTheme {
        RegistroObservacionContent(
            uiState = RegistroObservacionUiState(nombreObservador = "Vicente Ponce"),
            onAccesoDestinoSeleccionado = {},
            onClasificacionSeleccionada = {},
            onTieneComprasCambia = {},
            onCompaniaSeleccionada = {},
            onRangoEtarioSeleccionado = {},
            onLimpiar = {},
            onRegistrar = {}
        )
    }
}