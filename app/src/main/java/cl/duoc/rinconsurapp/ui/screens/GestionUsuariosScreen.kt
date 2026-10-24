package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.GestionUsuariosUiState
import cl.duoc.rinconsurapp.model.Usuario
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.viewmodel.GestionUsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    viewModel: GestionUsuariosViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GestionUsuariosContent(
        uiState = uiState,
        onEliminar = { viewModel.confirmarEliminacion(it) },
        onCancelarEliminacion = { viewModel.cancelarEliminacion() },
        onConfirmarEliminacion = { viewModel.eliminarUsuario() },
        onLimpiarErrorGlobal = { viewModel.limpiarErrorGlobal() },
        onEditar = { viewModel.abrirDialogoEditar(it) },
        onCancelarEdicion = { viewModel.cancelarEdicion() },
        onConfirmarEdicion = { viewModel.guardarEdicion() },
        onEditCorreoCambiado = { viewModel.onEditCorreoCambiado(it) },
        onEditTelefonoCambiado = { viewModel.onEditTelefonoCambiado(it) },
        onEditRolSeleccionado = { viewModel.onEditRolSeleccionado(it) },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosContent(
    uiState: GestionUsuariosUiState,
    onEliminar: (Usuario) -> Unit,
    onCancelarEliminacion: () -> Unit,
    onConfirmarEliminacion: () -> Unit,
    onLimpiarErrorGlobal: () -> Unit,
    onEditar: (Usuario) -> Unit,
    onCancelarEdicion: () -> Unit,
    onConfirmarEdicion: () -> Unit,
    onEditCorreoCambiado: (String) -> Unit,
    onEditTelefonoCambiado: (String) -> Unit,
    onEditRolSeleccionado: (String) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.titulo_gestion_usuarios), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.cd_volver))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primario, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.listaUsuarios.isEmpty()) {
                Text(
                    text = stringResource(R.string.lista_vacia_usuarios),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.listaUsuarios) { usuario ->
                        TarjetaUsuario(
                            usuario = usuario,
                            onEliminar = { onEliminar(usuario) },
                            onEditar = { onEditar(usuario) }
                        )
                    }
                }
            }
        }


        if (uiState.errorGlobal != null) {
            AlertDialog(
                onDismissRequest = onLimpiarErrorGlobal,
                title = { Text("Acción no permitida", fontWeight = FontWeight.Bold) },
                text = { Text(uiState.errorGlobal) },
                confirmButton = {
                    TextButton(onClick = onLimpiarErrorGlobal) {
                        Text("Entendido", color = Primario, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }


        if (uiState.mostrarDialogoEliminar) {
            AlertDialog(
                onDismissRequest = onCancelarEliminacion,
                title = { Text(stringResource(R.string.dialog_eliminar_usuario_titulo)) },
                text = { Text(stringResource(R.string.dialog_eliminar_usuario_msg)) },
                confirmButton = {
                    TextButton(onClick = onConfirmarEliminacion) {
                        Text(stringResource(R.string.btn_eliminar), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelarEliminacion) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }

        // Diálogo para Editar
        if (uiState.mostrarDialogoEditar && uiState.usuarioAEditar != null) {
            val esAdmin = uiState.usuarioAEditar.rolNombre == "Administrador"

            AlertDialog(
                onDismissRequest = onCancelarEdicion,
                title = { Text("Editar Usuario", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Modificando a: ${uiState.usuarioAEditar.nombreCompleto}", style = MaterialTheme.typography.bodyMedium)

                        OutlinedTextField(
                            value = uiState.editCorreo,
                            onValueChange = onEditCorreoCambiado,
                            label = { Text("Correo Electrónico") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.editTelefono,
                            onValueChange = onEditTelefonoCambiado,
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        SelectorDesplegableEdicion(
                            etiqueta = "Rol del Usuario",
                            opciones = uiState.listaRoles,
                            seleccionActual = uiState.editRolSeleccionado,
                            onOpcionSeleccionada = onEditRolSeleccionado,
                            habilitado = !esAdmin // Si es admin, bloqueamos el menú
                        )
                        if (esAdmin) {
                            Text(
                                text = "* El rol de Administrador no puede ser modificado.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (uiState.editError != null) {
                            Text(
                                text = uiState.editError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onConfirmarEdicion) {
                        Text("Guardar Cambios", color = Primario, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelarEdicion) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

@Composable
fun TarjetaUsuario(usuario: Usuario, onEliminar: () -> Unit, onEditar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_persona),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = usuario.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rut: ${formatearRut(usuario.rut)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Rol: ${usuario.rolNombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Correo: ${usuario.correo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Teléfono: ${usuario.telefono}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEditar) {
                    Icon(
                        painter = painterResource(R.drawable.ic_modificar_usuario),
                        contentDescription = "Editar usuario",
                        tint = Color(0xFF4CAF50)
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        painter = painterResource(R.drawable.ic_eliminar),
                        contentDescription = stringResource(R.string.btn_eliminar),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDesplegableEdicion(
    etiqueta: String,
    opciones: List<String>,
    seleccionActual: String,
    onOpcionSeleccionada: (String) -> Unit,
    habilitado: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido && habilitado,
        onExpandedChange = { if (habilitado) expandido = it }
    ) {
        OutlinedTextField(
            value = seleccionActual,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado, // Deshabilita el campo visualmente
            label = { Text(etiqueta) },
            trailingIcon = { if (habilitado) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onOpcionSeleccionada(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

private fun formatearRut(rut: String): String {
    val rutLimpio = rut.replace(Regex("[^0-9kK]"), "").uppercase()
    if (rutLimpio.length < 2) return rutLimpio
    val dv = rutLimpio.last()
    val numero = rutLimpio.dropLast(1)
    val numeroFormateado = numero.reversed().chunked(3).joinToString(".").reversed()
    return "$numeroFormateado-$dv"
}

@Preview(showBackground = true)
@Composable
fun GestionUsuariosPreview() {
    cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme {
        GestionUsuariosContent(
            uiState = GestionUsuariosUiState(
                listaUsuarios = listOf(
                    Usuario(
                        id = 1,
                        rut = "214297353",
                        contrasena = "",
                        nombreCompleto = "Vicente Ponce",
                        correo = "v.ponce@rinconsur.cl",
                        telefono = "987654321",
                        rolId = 2,
                        rolNombre = "Administrador"
                    )
                )
            ),
            onEliminar = {},
            onCancelarEliminacion = {},
            onConfirmarEliminacion = {},
            onLimpiarErrorGlobal = {},
            onEditar = {},
            onCancelarEdicion = {},
            onConfirmarEdicion = {},
            onEditCorreoCambiado = {},
            onEditTelefonoCambiado = {},
            onEditRolSeleccionado = {},
            onVolver = {}
        )
    }
}