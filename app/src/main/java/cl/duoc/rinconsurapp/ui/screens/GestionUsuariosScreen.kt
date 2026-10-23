package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
                            onEliminar = { onEliminar(usuario) }
                        )
                    }
                }
            }
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
    }
}

@Composable
fun TarjetaUsuario(usuario: Usuario, onEliminar: () -> Unit) {
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
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

private fun formatearRut(rut: String): String {
    val rutLimpio = rut.replace(Regex("[^0-9kK]"), "").uppercase()
    if (rutLimpio.length < 2) return rutLimpio
    val dv = rutLimpio.last()
    val numero = rutLimpio.dropLast(1)
    val numeroFormateado = numero.reversed().chunked(3).joinToString(".").reversed()
    return "$numeroFormateado-$dv"
}

// Preview de la Pantalla
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
                    ),
                    Usuario(
                        id = 2,
                        rut = "987654321",
                        contrasena = "",
                        nombreCompleto = "Juan Perez",
                        correo = "j.perez@rinconsur.cl",
                        telefono = "912345678",
                        rolId = 1,
                        rolNombre = "Observador"
                    )
                )
            ),
            onEliminar = {},
            onCancelarEliminacion = {},
            onConfirmarEliminacion = {},
            onVolver = {}
        )
    }
}