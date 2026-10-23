package cl.duoc.rinconsurapp.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.data.ConvertidorImagen
import cl.duoc.rinconsurapp.data.Vibracion
import cl.duoc.rinconsurapp.model.PerfilUsuarioUiState
import cl.duoc.rinconsurapp.permisos.Permisos
import cl.duoc.rinconsurapp.ui.components.AvatarCircular
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoTexto
import cl.duoc.rinconsurapp.ui.components.ImagenEncabezado
import cl.duoc.rinconsurapp.ui.components.TarjetaSeccion
import cl.duoc.rinconsurapp.ui.theme.Dimens
import cl.duoc.rinconsurapp.ui.theme.Exito
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.viewmodel.PerfilUsuarioViewModel
import kotlinx.coroutines.launch

@Composable
fun PerfilUsuarioScreen(
    viewModel: PerfilUsuarioViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onVolver: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoGaleriaDenegada = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = textoAjustes,
                duration = SnackbarDuration.Long
            )
            if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) viewModel.cambiarAvatarDesdeCamara(foto)
    }

    fun abrirCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirCamara() else avisarPermisoDenegado(textoCamaraDenegada)
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) {
            viewModel.cambiarAvatarDesdeGaleria(uri)
        }
    }

    fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            lanzadorGaleria.launch(intent)
        } catch (e: ActivityNotFoundException) {
        }
    }

    val pedirPermisoImagenes = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirGaleria() else avisarPermisoDenegado(textoGaleriaDenegada)
    }

    val foto = remember(uiState.avatar) {
        uiState.avatar?.let { bytes -> ConvertidorImagen.aBitmap(bytes)?.asImageBitmap() }
    }

    PerfilUsuarioContent(
        uiState = uiState,
        foto = foto,
        snackbarHostState = snackbarHostState,
        onTomarFoto = {
            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
            else pedirPermisoCamara.launch(Permisos.CAMARA)
        },
        onAbrirGaleria = {
            if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria()
            else pedirPermisoImagenes.launch(Permisos.IMAGENES)
        },
        alCambiarTelefono = { viewModel.alCambiarTelefono(it) },
        alCambiarCorreo = { viewModel.alCambiarCorreo(it) },
        alGuardar = {
            Vibracion.vibrar(context)
            viewModel.guardarCambios()
        },
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
    foto: ImageBitmap? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onTomarFoto: () -> Unit = {},
    onAbrirGaleria: () -> Unit = {},
    alCambiarTelefono: (String) -> Unit,
    alCambiarCorreo: (String) -> Unit,
    alGuardar: () -> Unit,
    alCerrarSesion: () -> Unit,
    onVolver: () -> Unit
) {
    var mostrarDialogoCerrar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.titulo_mi_perfil), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cd_volver)
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    modificador = Modifier.offset(y = 60.dp),
                    foto = foto
                )
            }

            Spacer(modifier = Modifier.height(70.dp))

            Text(
                text = stringResource(R.string.texto_cambiar_foto),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                modifier = Modifier.padding(top = Dimens.espacioChico, bottom = 16.dp)
            ) {
                BotonConIcono(
                    icono = R.drawable.ic_camera,
                    texto = stringResource(R.string.btn_camara),
                    onClick = onTomarFoto
                )
                BotonConIcono(
                    icono = R.drawable.ic_galeria,
                    texto = stringResource(R.string.btn_galeria),
                    onClick = onAbrirGaleria
                )
            }

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
                    onClick = { mostrarDialogoCerrar = true },
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
                text = { Text(stringResource(id = R.string.dialog_cerrar_sesion_msg)) },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoCerrar = false
                        alCerrarSesion()
                    }) {
                        Text(stringResource(id = R.string.btn_cerrar_sesion))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoCerrar = false }) {
                        Text(stringResource(id = R.string.btn_cancelar))
                    }
                }
            )
        }
    }
}

@Composable
private fun BotonConIcono(icono: Int, texto: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(
            painter = painterResource(icono),
            contentDescription = null,
            modifier = Modifier.size(Dimens.tamanoIcono)
        )
        Spacer(Modifier.width(Dimens.espacioChico))
        Text(texto)
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