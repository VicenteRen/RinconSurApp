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
import cl.duoc.rinconsurapp.model.DashboardAdminUiState
import cl.duoc.rinconsurapp.ui.components.GraficoBarrasDashboard
import cl.duoc.rinconsurapp.viewmodel.DashboardAdminViewModel
import java.util.Locale
import cl.duoc.rinconsurapp.ui.components.GraficoTortaDashboard
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DashboardAdminScreen(
    viewModel: DashboardAdminViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardAdminContent(
        estado = estado,
        onActualizar = viewModel::cargarEstadisticas
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminContent(
    estado: DashboardAdminUiState,
    onActualizar: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Rincón Sur") })
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
                Text("Acceso exclusivo para administradores")
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
                    "Dashboard de recuentos",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    "Estadísticas globales de observación",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaDato(
                        "Observados",
                        estado.totalRegistros.toString(),
                        Modifier.weight(1f)
                    )

                    TarjetaDato(
                        "Visitas",
                        estado.totalVisitas.toString(),
                        Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaDato(
                        "Tránsito",
                        estado.totalTransito.toString(),
                        Modifier.weight(1f)
                    )

                    TarjetaDato(
                        "No determinado",
                        estado.totalNoDeterminado.toString(),
                        Modifier.weight(1f)
                    )
                }

                val totalClasificados = estado.totalVisitas + estado.totalTransito

                val porcentajeVisitas = if (totalClasificados > 0) {
                    estado.totalVisitas.toFloat() / totalClasificados * 100f
                } else {
                    null
                }

                val porcentajeTransito = estado.porcentajeTransito

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
                                    porcentajeTransito?.let {
                                        String.format(
                                            Locale.forLanguageTag("es-CL"),
                                            "%.1f %%",
                                            it
                                        )
                                    } ?: "Sin datos",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Visitas")

                                Text(
                                    porcentajeVisitas?.let {
                                        String.format(
                                            Locale.forLanguageTag("es-CL"),
                                            "%.1f %%",
                                            it
                                        )
                                    } ?: "Sin datos",
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
                            "Registros por rango etario",
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
                            "Distribución por clasificación",
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
                                "Estadísticas actualizadas"
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

@Composable
private fun TarjetaDato(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
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