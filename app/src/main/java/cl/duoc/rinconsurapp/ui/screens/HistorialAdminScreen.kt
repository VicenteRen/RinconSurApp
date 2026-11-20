
package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.RegistroObservacion
import cl.duoc.rinconsurapp.ui.components.DialogoAnularObservacion
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.PrimarioOscuro
import cl.duoc.rinconsurapp.viewmodel.HistorialAdminViewModel
import kotlinx.coroutines.launch

@Composable
fun HistorialAdminScreen(
    onAbrirMenu: () -> Unit = {},
    viewModel: HistorialAdminViewModel = viewModel()
) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val autorizado by viewModel.autorizado.collectAsStateWithLifecycle()

    HistorialAdminContent(
        registros = registros,
        autorizado = autorizado,
        alActualizar = {
            viewModel.cargarRegistros()
        },
        alAnular = { id, motivo, contrasena, onResultado ->
            viewModel.anularRegistro(
                idRegistro = id,
                motivo = motivo,
                contrasena = contrasena,
                onResultado = onResultado
            )
        },
        onAbrirMenu = onAbrirMenu
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAdminContent(
    registros: List<RegistroObservacion>,
    autorizado: Boolean,
    alActualizar: () -> Unit,
    alAnular: (
        Long,
        String,
        String,
        (Boolean, String) -> Unit
    ) -> Unit,
    onAbrirMenu: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val mensajeActualizado =
        stringResource(R.string.historial_actualizado)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.historial_admin_titulo)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = stringResource(
                                R.string.dashboard_abrir_menu
                            )
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
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!autorizado) {
                Text(
                    stringResource(R.string.historial_admin_acceso)
                )
            } else {

                if (registros.isEmpty()) {
                    Text(
                        stringResource(R.string.historial_sin_registros)
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.historial_total_registros,
                            registros.size
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = registros,
                            key = { it.id }
                        ) { registro ->

                            TarjetaRegistroAdmin(
                                registro = registro,
                                alAnular = {
                                        motivo, contrasena, onResultado ->

                                    alAnular(
                                        registro.id,
                                        motivo,
                                        contrasena,
                                        onResultado
                                    )
                                },
                                onExito = { mensaje ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            mensaje
                                        )
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
                    Text(
                        stringResource(R.string.historial_actualizar)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaRegistroAdmin(
    registro: RegistroObservacion,
    alAnular: (
        String,
        String,
        (Boolean, String) -> Unit
    ) -> Unit,
    onExito: (String) -> Unit
) {
    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

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

            Text(
                stringResource(
                    R.string.historial_rango_etario,
                    registro.rangoEtario
                )
            )

            Text(
                stringResource(
                    R.string.historial_compania,
                    registro.compania
                )
            )

            if (registro.accesoDestino != null) {
                Text(
                    stringResource(
                        R.string.historial_destino,
                        registro.accesoDestino
                    )
                )
            }

            Text(
                stringResource(
                    R.string.historial_fecha_hora,
                    registro.fechaHora
                )
            )

            Text(
                stringResource(
                    R.string.historial_registrado_por,
                    registro.nombreObservador
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    mostrarConfirmacion = true
                }
            ) {
                Text("Anular observación")
            }
        }
    }

    if (mostrarConfirmacion) {
        DialogoAnularObservacion(
            onCerrar = {
                mostrarConfirmacion = false
            },
            onConfirmar = alAnular,
            onExito = onExito
        )
    }
}
