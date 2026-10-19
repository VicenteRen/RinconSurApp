package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.model.RegistroObservacion
import cl.duoc.rinconsurapp.viewmodel.HistorialObservadorViewModel
import kotlinx.coroutines.launch

@Composable
fun HistorialObservadorScreen(
    onAbrirMenu: () -> Unit = {},
    viewModel: HistorialObservadorViewModel = viewModel()
) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val autorizado by viewModel.autorizado.collectAsStateWithLifecycle()

    HistorialObservadorContent(
        registros = registros,
        autorizado = autorizado,
        alActualizar = { viewModel.cargarRegistros() },
        alEliminar = { id -> viewModel.eliminarRegistro(id) },
        onAbrirMenu = onAbrirMenu
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialObservadorContent(
    registros: List<RegistroObservacion>,
    autorizado: Boolean,
    alActualizar: () -> Unit,
    alEliminar: (Long) -> Boolean,
    onAbrirMenu: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi historial de observaciones") },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(cl.duoc.rinconsurapp.R.drawable.ic_menu),
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!autorizado) {
                Text("Acceso exclusivo para observadores")
            } else {
                if (registros.isEmpty()) {
                    Text("Todavía no tienes observaciones registradas.")
                } else {
                    Text(
                        text = "Mis registros: ${registros.size}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(registros, key = { it.id }) { registro ->
                            TarjetaRegistroObservador(
                                registro = registro,
                                alEliminar = { id ->
                                    if (alEliminar(id)) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Observación eliminada correctamente"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        alActualizar()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Historial actualizado"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar historial")
                }
            }
        }
    }
}

@Composable
private fun TarjetaRegistroObservador(
    registro: RegistroObservacion,
    alEliminar: (Long) -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    "¿Deseas eliminar este registro? Esta acción no se puede deshacer."
                )
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