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
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.RestablecerContrasenaUiState
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoContrasena
import cl.duoc.rinconsurapp.ui.components.CampoTexto
import cl.duoc.rinconsurapp.ui.theme.Exito
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.viewmodel.RestablecerContrasenaViewModel
import androidx.compose.material3.ExposedDropdownMenuAnchorType
@Composable
fun RestablecerContrasenaScreen(
    viewModel: RestablecerContrasenaViewModel = viewModel(),
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    RestablecerContrasenaContent(
        uiState = estado,
        onRutCambiado = viewModel::onRutCambiado,
        onPreguntaSeleccionada = viewModel::onPreguntaSeleccionada,
        onRespuestaCambiada = viewModel::onRespuestaCambiada,
        onNuevaContrasenaCambiada = viewModel::onNuevaContrasenaCambiada,
        onConfirmarContrasenaCambiada =
            viewModel::onConfirmarContrasenaCambiada,
        onVerificar = viewModel::verificarIdentidad,
        onRestablecer = viewModel::intentarRestablecerContrasena,
        onVolver = {
            if (estado.identidadVerificada) {
                viewModel.volverAlPrimerPaso()
            } else {
                onVolver()
            }
        },
        onDialogoCerrado = {
            viewModel.onDialogoExitoCerrado()
            onVolver()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestablecerContrasenaContent(
    uiState: RestablecerContrasenaUiState,
    onRutCambiado: (String) -> Unit,
    onPreguntaSeleccionada: (String) -> Unit,
    onRespuestaCambiada: (String) -> Unit,
    onNuevaContrasenaCambiada: (String) -> Unit,
    onConfirmarContrasenaCambiada: (String) -> Unit,
    onVerificar: () -> Unit,
    onRestablecer: () -> Unit,
    onVolver: () -> Unit,
    onDialogoCerrado: () -> Unit
) {
    val pasoDos = uiState.identidadVerificada

    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (pasoDos) "Nueva contraseña"
                        else "Recuperar contraseña",
                        fontWeight = FontWeight.Bold
                    )
                },
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
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (pasoDos) "Paso 2 de 2"
                else "Paso 1 de 2",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (!pasoDos) {
                Text(
                    "Verifica tu identidad para recuperar el acceso a tu cuenta."
                )

                CampoTexto(
                    valor = uiState.rut,
                    onValorCambia = onRutCambiado,
                    etiqueta = stringResource(R.string.etiqueta_rut),
                    icono = R.drawable.ic_persona,
                    error = uiState.rutError
                )

                var expandido by remember {
                    mutableStateOf(false)
                }

                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = it }
                ) {
                    OutlinedTextField(
                        value = uiState.preguntaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pregunta de seguridad") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandido
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                            )
                    )

                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        uiState.listaPreguntas.forEach { pregunta ->
                            DropdownMenuItem(
                                text = { Text(pregunta) },
                                onClick = {
                                    onPreguntaSeleccionada(pregunta)
                                    expandido = false
                                }
                            )
                        }
                    }
                }

                CampoTexto(
                    valor = uiState.respuestaSeguridad,
                    onValorCambia = onRespuestaCambiada,
                    etiqueta = "Respuesta de seguridad",
                    icono = R.drawable.ic_candado,
                    error = uiState.respuestaError
                )

                BotonPrincipal(
                    texto = if (uiState.verificando)
                        "Verificando..." else "Continuar",
                    onClick = onVerificar,
                    habilitado = !uiState.verificando &&
                            uiState.rutError == null &&
                            uiState.rut.isNotBlank() &&
                            uiState.respuestaSeguridad.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    "Identidad verificada. Crea tu nueva contraseña."
                )

                CampoContrasena(
                    valor = uiState.nuevaContrasena,
                    onValorCambia = onNuevaContrasenaCambiada,
                    etiqueta = stringResource(
                        R.string.etiqueta_nueva_contrasena
                    ),
                    visible = mostrarContrasena,
                    onAlternarVisible = { mostrarContrasena = !mostrarContrasena },
                    error = null
                )

                CampoContrasena(
                    valor = uiState.confirmarContrasena,
                    onValorCambia = onConfirmarContrasenaCambiada,
                    etiqueta = stringResource(
                        R.string.etiqueta_confirmar_contrasena
                    ),
                    visible = mostrarConfirmacion,
                    onAlternarVisible = { mostrarConfirmacion = !mostrarConfirmacion },
                    error = uiState.contrasenaError
                )

                Text(
                    "Requisitos de contraseña",
                    style = MaterialTheme.typography.titleMedium
                )

                RequisitoTexto(
                    "Entre 8 y 64 caracteres",
                    uiState.tieneLongitudValida
                )

                RequisitoTexto(
                    "Al menos un número",
                    uiState.tieneNumero
                )

                RequisitoTexto(
                    "Al menos una mayúscula",
                    uiState.tieneMayuscula
                )

                RequisitoTexto(
                    "Al menos un carácter especial",
                    uiState.tieneEspecial
                )

                BotonPrincipal(
                    texto = if (uiState.verificando)
                        "Guardando..." else "Restablecer contraseña",
                    onClick = onRestablecer,
                    habilitado = uiState.formularioValido &&
                            !uiState.verificando,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            uiState.errorGeneral?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }
        }

        if (uiState.restablecimientoExitoso) {
            AlertDialog(
                onDismissRequest = onDialogoCerrado,
                title = { Text("Contraseña restablecida") },
                text = {
                    Text(
                        "Tu contraseña se actualizó correctamente. Ya puedes iniciar sesión."
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDialogoCerrado) {
                        Text("Ir al inicio de sesión")
                    }
                }
            )
        }
    }
}

@Composable
fun RequisitoTexto(
    texto: String,
    cumplido: Boolean
) {
    Text(
        text = "${if (cumplido) "✓" else "○"} $texto",
        style = MaterialTheme.typography.bodyMedium,
        color = if (cumplido)
            Exito else MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}