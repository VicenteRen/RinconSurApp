package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rinconsurapp.model.RegistroObservacion
import cl.duoc.rinconsurapp.viewmodel.HistorialAdminViewModel

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun HistorialAdminScreen(
    viewModel: HistorialAdminViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val autorizado by viewModel.autorizado.collectAsStateWithLifecycle()

    HistorialAdminContent(
        registros = registros,
        autorizado = autorizado,
        alActualizar = { viewModel.cargarRegistros() },
        alEliminar = { id -> viewModel.eliminarRegistro(id) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAdminContent(
    registros: List<RegistroObservacion>,
    autorizado: Boolean,
    alActualizar: () -> Unit,
    alEliminar: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de observaciones") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!autorizado) {
                Text("Acceso exclusivo para administradores")
            } else {
                if (registros.isEmpty()) {
                    Text("Todavía no hay observaciones registradas.")
                } else {
                    Text(
                        text = "Total de registros: ${registros.size}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(registros) { registro ->
                            TarjetaRegistroAdmin(
                                registro = registro,
                                alEliminar = alEliminar)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = alActualizar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar historial")
                }
            }
        }
    }
}



@Composable
private fun TarjetaRegistroAdmin(
    registro: RegistroObservacion,
    alEliminar: (Long) -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = registro.clasificacion,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Rango etario: ${registro.rangoEtario}")
            Text("Compañía: ${registro.compania}")

            if (registro.accesoDestino != null) {
                Text("Destino: ${registro.accesoDestino}")
            }

            Text("Hora: ${registro.fechaHora}")
            Text("Registrado por: ${registro.nombreObservador}")

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { mostrarConfirmacion = true }
            ) {
                Text("Eliminar")
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar observación") },
            text = {
                Text("¿Estás seguro de que deseas eliminar este registro? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        alEliminar(registro.id)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacion = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
