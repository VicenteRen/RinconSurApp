package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import cl.duoc.rinconsurapp.model.PerfilUsuarioUiState
import cl.duoc.rinconsurapp.ui.components.AvatarCircular
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoTexto
import cl.duoc.rinconsurapp.ui.components.ImagenEncabezado
import cl.duoc.rinconsurapp.ui.components.TarjetaSeccion
import cl.duoc.rinconsurapp.ui.theme.Exito
import cl.duoc.rinconsurapp.ui.theme.NegroIconos
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.ui.theme.TextoClaro
import cl.duoc.rinconsurapp.viewmodel.PerfilUsuarioViewModel

@Composable
fun PerfilUsuarioScreen(
    viewModel: PerfilUsuarioViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onVolver: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PerfilUsuarioContent(
        uiState = uiState,
        alCambiarTelefono = { viewModel.alCambiarTelefono(it) },
        alCambiarCorreo = { viewModel.alCambiarCorreo(it) },
        alGuardar = { viewModel.guardarCambios() },
        alCerrarSesion = {
            viewModel.cerrarSesion()
            onCerrarSesion()
        },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioContent(
    uiState: PerfilUsuarioUiState,
    alCambiarTelefono: (String) -> Unit,
    alCambiarCorreo: (String) -> Unit,
    alGuardar: () -> Unit,
    alCerrarSesion: () -> Unit,
    onVolver: () -> Unit
) {
    // Variable para controlar la visibilidad del diálogo de cierre de sesión
    var mostrarDialogoCerrar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primario,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                ImagenEncabezado(
                    descripcion = stringResource(id = R.string.cd_fondo_perfil),
                    titulo = uiState.nombreCompleto,
                    subtitulo = "${stringResource(id = R.string.prefijo_cargo)} ${uiState.cargo}"
                )

                AvatarCircular(
                    imagen = R.drawable.avatar_usuario,
                    descripcion = stringResource(id = R.string.cd_foto_perfil),
                    modificador = Modifier.offset(y = 60.dp)
                )
            }

            Spacer(modifier = Modifier.height(70.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                TarjetaSeccion(titulo = stringResource(id = R.string.titulo_info_contacto)) {
                    Spacer(modifier = Modifier.height(16.dp))

                    CampoTexto(
                        valor = uiState.correoContacto,
                        onValorCambia = alCambiarCorreo,
                        etiqueta = stringResource(id = R.string.etiqueta_correo),
                        icono = android.R.drawable.ic_dialog_email,
                        error = uiState.errorCorreo
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CampoTexto(
                        valor = uiState.telefonoContacto,
                        onValorCambia = alCambiarTelefono,
                        etiqueta = stringResource(id = R.string.etiqueta_telefono),
                        icono = android.R.drawable.ic_menu_call,
                        error = uiState.errorTelefono
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    BotonPrincipal(
                        texto = stringResource(id = R.string.btn_actualizar_datos),
                        onClick = alGuardar,
                        habilitado = uiState.esValido,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.informacionModificadaExitosamente) {
                        Text(
                            text = stringResource(id = R.string.msg_actualizacion_exitosa),
                            color = Exito,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                BotonPrincipal(
                    texto = stringResource(id = R.string.btn_cerrar_sesion),
                    onClick = { mostrarDialogoCerrar = true }, // Cambiado: Ahora abre el diálogo
                    habilitado = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.cerrarSesionExitoso) {
                    Text(
                        text = stringResource(id = R.string.msg_cerrando_sesion),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 24.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }


        if (mostrarDialogoCerrar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoCerrar = false },
                title = { Text(stringResource(id = R.string.btn_cerrar_sesion)) },
                text = { Text("¿Estás seguro de que deseas cerrar la sesión actual?") },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoCerrar = false
                        alCerrarSesion() // Llama al ViewModel si confirma
                    }) {
                        Text("Sí, cerrar sesión")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoCerrar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilUsuarioPreview() {
    RinconSurAppTheme {
        PerfilUsuarioContent(
            uiState = PerfilUsuarioUiState(),
            alCambiarTelefono = {},
            alCambiarCorreo = {},
            alGuardar = {},
            alCerrarSesion = {},
            onVolver = {}
        )
    }
}