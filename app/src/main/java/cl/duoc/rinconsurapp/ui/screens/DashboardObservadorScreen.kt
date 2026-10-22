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
                title = { Text("Rincón Sur") }, // o el título que ella puso en Historial
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(cl.duoc.rinconsurapp.R.drawable.ic_menu),
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
                Text("Acceso exclusivo para observadores")
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
                    "Mi Dashboard de recuentos",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    "Estadísticas de mis observaciones",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaDatoObservador(
                        "Observados",
                        estado.totalRegistros.toString(),
                        Modifier.weight(1f)
                    )

                    TarjetaDatoObservador(
                        "Visitas",
                        estado.totalVisitas.toString(),
                        Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaDatoObservador(
                        "Tránsito",
                        estado.totalTransito.toString(),
                        Modifier.weight(1f)
                    )

                    TarjetaDatoObservador(
                        "No determinado",
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
                            "Comportamiento observado",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tránsito")

                                Text(
                                    formatoPorcentajeObservador(
                                        estado.porcentajeTransito
                                    ),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Visitas")

                                Text(
                                    formatoPorcentajeObservador(
                                        estado.porcentajeVisitas
                                    ),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Mis registros por rango etario",
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
                            "Mis registros por clasificación",
                            style = MaterialTheme.typography.titleMedium
                        )

                        GraficoTortaDashboard(
                            visitas = estado.totalVisitas,
                            transito = estado.totalTransito,
                            noDeterminado = estado.totalNoDeterminado
                        )
                    }
                }

                Button(
                    onClick = {
                        onActualizar()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Estadísticas actualizadas correctamente"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar estadísticas")
                }
            }
        }
    }
}

private fun formatoPorcentajeObservador(valor: Float?): String {
    return valor?.let {
        String.format(
            Locale.forLanguageTag("es-CL"),
            "%.1f %%",
            it
        )
    } ?: "Sin datos"
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