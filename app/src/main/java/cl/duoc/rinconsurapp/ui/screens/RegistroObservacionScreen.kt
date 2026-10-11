package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.model.*
import cl.duoc.rinconsurapp.ui.components.*
import cl.duoc.rinconsurapp.ui.theme.Dimens
import cl.duoc.rinconsurapp.viewmodel.RegistroObservacionViewModel

@Composable
fun RegistroObservacionScreen(
    viewModel: RegistroObservacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegistroObservacionContent(
        uiState = uiState,
        onClasificacionSeleccionada = viewModel::onClasificacionSeleccionada,
        onRangoEtarioSeleccionado = viewModel::onRangoEtarioSeleccionado,
        onCompaniaSeleccionada = viewModel::onCompaniaSeleccionada,
        onTieneComprasCambia = viewModel::onTieneComprasCambia,
        onAccesoDestinoSeleccionado = viewModel::onAccesoDestinoSeleccionado,
        onLimpiar = viewModel::onLimpiar,
        onRegistrar = viewModel::onRegistrar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroObservacionContent(
    uiState: RegistroObservacionUiState,
    onClasificacionSeleccionada: (Clasificacion) -> Unit,
    onRangoEtarioSeleccionado: (RangoEtario) -> Unit,
    onCompaniaSeleccionada: (Compania) -> Unit,
    onTieneComprasCambia: (Boolean) -> Unit,
    onAccesoDestinoSeleccionado: (Acceso) -> Unit,
    onLimpiar: () -> Unit,
    onRegistrar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Registro - Observador") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaSeccion(titulo = "01. Clasificación") {
                GrupoRadio(
                    opciones = Clasificacion.entries,
                    seleccionada = uiState.clasificacion,
                    onSeleccionar = onClasificacionSeleccionada,
                    textoDe = { it.etiqueta }
                )
            }

            TarjetaSeccion(titulo = "02. Rango Etario") {
                GrupoRadio(
                    opciones = RangoEtario.entries,
                    seleccionada = uiState.rangoEtario,
                    onSeleccionar = onRangoEtarioSeleccionado,
                    textoDe = { it.etiqueta }
                )
            }

            TarjetaSeccion(titulo = "03. Compañía") {
                GrupoRadio(
                    opciones = Compania.entries,
                    seleccionada = uiState.compania,
                    onSeleccionar = onCompaniaSeleccionada,
                    textoDe = { it.etiqueta }
                )
            }

            TarjetaSeccion(titulo = "04. Variables Adicionales") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Trae compras de otra tienda?",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.tieneComprasExternas,
                        onCheckedChange = onTieneComprasCambia
                    )
                }
            }

            if (uiState.clasificacion == Clasificacion.TRANSITO) {
                TarjetaSeccion(titulo = "05. Hacia qué acceso se dirige:") {
                    GrupoRadio(
                        opciones = Acceso.entries,
                        seleccionada = uiState.accesoDestino ?: Acceso.A1,
                        onSeleccionar = onAccesoDestinoSeleccionado,
                        textoDe = { it.etiqueta }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BotonSecundario(
                    texto = "Limpiar",
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = "Guardar",
                    onClick = onRegistrar,
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.registroExitoso) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = cl.duoc.rinconsurapp.R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.tamanoIcono)
                        )
                        Text(
                            text = "¡Registro guardado correctamente!",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true)
@Composable
private fun RegistroObservacionPreview() {
    MaterialTheme {
        RegistroObservacionContent(
            uiState = RegistroObservacionUiState(),
            onClasificacionSeleccionada = {},
            onRangoEtarioSeleccionado = {},
            onCompaniaSeleccionada = {},
            onTieneComprasCambia = {},
            onAccesoDestinoSeleccionado = {},
            onLimpiar = {},
            onRegistrar = {}
        )
    }
}