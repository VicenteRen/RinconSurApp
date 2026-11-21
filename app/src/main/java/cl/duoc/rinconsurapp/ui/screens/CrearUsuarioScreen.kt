package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.CrearUsuarioUiState
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoTexto
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.viewmodel.CrearUsuarioViewModel

@Composable
fun CrearUsuarioScreen(
    viewModel: CrearUsuarioViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onAbrirMenu: () -> Unit,
    onUsuarioCreado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textoRolAdmin = stringResource(R.string.rol_administrador)
    val opcionesRoles = listOf(
        stringResource(R.string.rol_observador),
        stringResource(R.string.rol_administrador)
    )

    LaunchedEffect(Unit) {
        viewModel.inicializarRoles(opcionesRoles)
    }

    CrearUsuarioContent(
        uiState = uiState,
        onNombreCambiado = { viewModel.onNombreCambiado(it) },
        onRutCambiado = { viewModel.onRutCambiado(it) },
        onCorreoCambiado = { viewModel.onCorreoCambiado(it) },
        onTelefonoCambiado = { viewModel.onTelefonoCambiado(it) },
        onRolSeleccionado = { viewModel.onRolSeleccionado(it) },
        onCrearUsuario = { viewModel.intentarCrearUsuario(textoRolAdmin) },
        onDialogoExitoCerrado = {
            viewModel.onDialogoExitoCerrado()
            onUsuarioCreado()
        },
        onAbrirMenu = onAbrirMenu
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioContent(
    uiState: CrearUsuarioUiState,
    onNombreCambiado: (String) -> Unit,
    onRutCambiado: (String) -> Unit,
    onCorreoCambiado: (String) -> Unit,
    onTelefonoCambiado: (String) -> Unit,
    onRolSeleccionado: (String) -> Unit,
    onCrearUsuario: () -> Unit,
    onDialogoExitoCerrado: () -> Unit,
    onAbrirMenu: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.titulo_crear_usuario), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = stringResource(R.string.dashboard_abrir_menu)
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.subtitulo_crear_usuario),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CampoTexto(
                valor = uiState.nombre,
                onValorCambia = onNombreCambiado,
                etiqueta = stringResource(R.string.etiqueta_nombre),
                icono = R.drawable.ic_persona,
                error = null
            )

            CampoTexto(
                valor = uiState.rut,
                onValorCambia = onRutCambiado,
                etiqueta = stringResource(R.string.etiqueta_rut),
                icono = R.drawable.ic_persona,
                error = uiState.rutError
            )

            CampoTexto(
                valor = uiState.correo,
                onValorCambia = onCorreoCambiado,
                etiqueta = stringResource(R.string.etiqueta_correo),
                icono = R.drawable.ic_correo,
                error = uiState.correoError
            )

            CampoTexto(
                valor = uiState.telefono,
                onValorCambia = onTelefonoCambiado,
                etiqueta = stringResource(R.string.etiqueta_telefono),
                icono = R.drawable.ic_telefono,
                error = null
            )

            SelectorDesplegable(
                etiqueta = stringResource(R.string.etiqueta_rol),
                opciones = uiState.listaRoles,
                seleccionActual = uiState.rolSeleccionado,
                onOpcionSeleccionada = onRolSeleccionado
            )

            if (uiState.errorGeneral != null) {
                Text(
                    text = uiState.errorGeneral!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            BotonPrincipal(
                texto = if (uiState.cargando) "Enviando credenciales..." else stringResource(R.string.btn_crear_usuario),
                onClick = onCrearUsuario,
                habilitado = uiState.formularioValido && !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        if (uiState.creacionExitosa) {
            AlertDialog(
                onDismissRequest = onDialogoExitoCerrado,
                title = { Text(stringResource(R.string.titulo_crear_usuario)) },
                text = {
                    Text(
                        "${stringResource(R.string.msg_usuario_creado)}\n\n" +
                                "Se envió una contraseña temporal al correo ${uiState.correo.trim()}."
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDialogoExitoCerrado) {
                        Text(stringResource(R.string.btn_aceptar))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccionActual: String,
    onOpcionSeleccionada: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {
        OutlinedTextField(
            value = seleccionActual,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
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