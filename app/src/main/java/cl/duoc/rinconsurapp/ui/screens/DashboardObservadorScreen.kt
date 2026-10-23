package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.model.DashboardObservadorUiState
import cl.duoc.rinconsurapp.ui.components.GraficoBarrasDashboard
import cl.duoc.rinconsurapp.ui.components.GraficoTortaDashboard
import cl.duoc.rinconsurapp.viewmodel.DashboardObservadorViewModel
import java.util.Locale
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import cl.duoc.rinconsurapp.ui.theme.Primario
import kotlinx.coroutines.launch
import androidx.compose.material3.ButtonDefaults
import cl.duoc.rinconsurapp.ui.theme.PrimarioOscuro
import androidx.compose.ui.res.stringResource
import cl.duoc.rinconsurapp.R

@Composable
fun DashboardObservadorScreen(
    onAbrirMenu: () -> Unit = {},
    viewModel: DashboardObservadorViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardObservadorContent(
        estado = estado,
        onActualizar = viewModel::cargarEstadisticas,
        onAbrirMenu = onAbrirMenu
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardObservadorContent(
    estado: DashboardObservadorUiState,
    onActualizar: () -> Unit,
    onAbrirMenu: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.dashboard_nombre_app))
                },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(cl.duoc.rinconsurapp.R.drawable.ic_menu),
                            contentDescription = stringResource(R.string.dashboard_abrir_menu)
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
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingInterno ->

        if (!estado.autorizado) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.dashboard_observador_acceso))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.dashboard_observador_titulo),
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    stringResource(R.string.dashboard_observador_subtitulo),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaDatoObservador(
                        stringResource(R.string.dashboard_observados),
                        estado.totalRegistros.toString(),
                        Modifier.weight(1f)
                    )

                    TarjetaDatoObservador(
                        stringResource(R.string.dashboard_visitas),
                        estado.totalVisitas.toString(),
                        Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaDatoObservador(
                        stringResource(R.string.dashboard_transito),
                        estado.totalTransito.toString(),
                        Modifier.weight(1f)
                    )

                    TarjetaDatoObservador(
                        stringResource(R.string.dashboard_no_determinado),
                        estado.totalNoDeterminado.toString(),
                        Modifier.weight(1f)
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.dashboard_comportamiento),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.dashboard_transito))

                                Text(
                                    formatoPorcentajeObservador(
                                        estado.porcentajeTransito
                                    ),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.dashboard_visitas))

                                Text(
                                    formatoPorcentajeObservador(
                                        estado.porcentajeVisitas
                                    ),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column {
                            Text(stringResource(R.string.dashboard_no_determinado))

                            val porcentajeNoDeterminado = if (estado.totalRegistros > 0) {
                                estado.totalNoDeterminado.toFloat() /
                                        estado.totalRegistros * 100f
                            } else {
                                null
                            }

                            Text(
                                formatoPorcentajeObservador(porcentajeNoDeterminado),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.dashboard_observador_rango_etario),
                            style = MaterialTheme.typography.titleMedium
                        )

                        GraficoBarrasDashboard(
                            datos = estado.registrosPorEdad
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.dashboard_observador_clasificacion),
                            style = MaterialTheme.typography.titleMedium
                        )

                        GraficoTortaDashboard(
                            visitas = estado.totalVisitas,
                            transito = estado.totalTransito,
                            noDeterminado = estado.totalNoDeterminado
                        )
                    }
                }

                val mensajeActualizado = stringResource(
                    R.string.dashboard_observador_estadisticas_actualizadas
                )

                Button(
                    onClick = {
                        onActualizar()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                mensajeActualizado
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimarioOscuro,
                        contentColor = Color.White

                    )
                ) {
                    Text(stringResource(R.string.dashboard_actualizar_estadisticas))
                }
            }
        }
    }
}

@Composable
private fun formatoPorcentajeObservador(valor: Float?): String {
    return valor?.let {
        String.format(
            Locale.forLanguageTag("es-CL"),
            "%.1f %%",
            it
        )
    } ?: stringResource(R.string.dashboard_sin_datos)
}

@Composable
private fun TarjetaDatoObservador(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                valor,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}