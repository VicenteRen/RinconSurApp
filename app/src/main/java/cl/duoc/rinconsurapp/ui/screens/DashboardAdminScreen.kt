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
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Rincón Sur") })
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

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Porcentaje de tránsito observado",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            estado.porcentajeTransito?.let {
                                String.format(
                                    Locale.forLanguageTag("es-CL"),
                                    "%.1f %%",
                                    it
                                )
                            } ?: "Sin datos",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
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

                Button(
                    onClick = onActualizar,
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