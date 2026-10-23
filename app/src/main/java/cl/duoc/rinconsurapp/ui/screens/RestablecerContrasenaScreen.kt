package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import cl.duoc.rinconsurapp.model.RestablecerContrasenaUiState
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoContrasena
import cl.duoc.rinconsurapp.ui.components.CampoTexto
import cl.duoc.rinconsurapp.ui.theme.Exito
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.viewmodel.RestablecerContrasenaViewModel

@Composable
fun RestablecerContrasenaScreen(
    viewModel: RestablecerContrasenaViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RestablecerContrasenaContent(
        uiState = uiState,
        onRutCambiado = { viewModel.onRutCambiado(it) },
        onPreguntaSeleccionada = { viewModel.onPreguntaSeleccionada(it) },
        onRespuestaCambiada = { viewModel.onRespuestaCambiada(it) },
        onNuevaContrasenaCambiada = { viewModel.onNuevaContrasenaCambiada(it) },
        onConfirmarContrasenaCambiada = { viewModel.onConfirmarContrasenaCambiada(it) },
        onAlternarVisibilidad = { viewModel.alternarVisibilidadContrasenas() },
        onRestablecer = { viewModel.intentarRestablecerContrasena() },
        onVolver = onVolver,
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
    onAlternarVisibilidad: () -> Unit,
    onRestablecer: () -> Unit,
    onVolver: () -> Unit,
    onDialogoCerrado: () -> Unit
) {
    val opcionesPreguntas = listOf(
        stringResource(R.string.preg_mascota),
        stringResource(R.string.preg_amigo),
        stringResource(R.string.preg_ciudad)
    )

    if (uiState.preguntaSeleccionada.isEmpty() && opcionesPreguntas.isNotEmpty()) {
        onPreguntaSeleccionada(opcionesPreguntas[0])
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.titulo_restablecer), fontWeight = FontWeight.Bold) },
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
                text = stringResource(R.string.etiqueta_seleccione_pregunta),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CampoTexto(
                valor = uiState.rut,
                onValorCambia = onRutCambiado,
                etiqueta = stringResource(R.string.etiqueta_rut),
                icono = R.drawable.ic_persona,
                error = uiState.rutError
            )

            var expandido by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it }
            ) {
                OutlinedTextField(
                    value = uiState.preguntaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.etiqueta_seleccione_pregunta)) },
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
                    opcionesPreguntas.forEach { pregunta ->
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
                etiqueta = stringResource(R.string.etiqueta_respuesta),
                icono = R.drawable.ic_candado,
                error = uiState.respuestaError
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.titulo_crear_segura),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CampoContrasena(
                valor = uiState.nuevaContrasena,
                onValorCambia = onNuevaContrasenaCambiada,
                etiqueta = stringResource(R.string.etiqueta_nueva_contrasena),
                visible = uiState.contrasenasVisibles,
                onAlternarVisible = onAlternarVisibilidad,
                error = null
            )

            CampoContrasena(
                valor = uiState.confirmarContrasena,
                onValorCambia = onConfirmarContrasenaCambiada,
                etiqueta = stringResource(R.string.etiqueta_confirmar_contrasena),
                visible = uiState.contrasenasVisibles,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.contrasenaError
            )

            Column(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                Text(
                    text = stringResource(R.string.titulo_requisitos_contrasena),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                RequisitoTexto(texto = stringResource(R.string.req_longitud), cumplido = uiState.tieneLongitudValida)
                RequisitoTexto(texto = stringResource(R.string.req_numero), cumplido = uiState.tieneNumero)
                RequisitoTexto(texto = stringResource(R.string.req_mayuscula), cumplido = uiState.tieneMayuscula)
                RequisitoTexto(texto = stringResource(R.string.req_sin_especiales), cumplido = uiState.sinCaracteresEspeciales)
            }

            if (uiState.errorGeneral != null) {
                Text(
                    text = uiState.errorGeneral,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_cambiar_contrasena),
                onClick = onRestablecer,
                habilitado = uiState.formularioValido,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.restablecimientoExitoso) {
            AlertDialog(
                onDismissRequest = onDialogoCerrado,
                title = { Text(stringResource(R.string.titulo_restablecer)) },
                text = { Text(stringResource(R.string.msg_contrasena_restablecida)) },
                confirmButton = {
                    TextButton(onClick = onDialogoCerrado) {
                        Text(stringResource(R.string.btn_ir_login))
                    }
                }
            )
        }
    }
}

@Composable
fun RequisitoTexto(texto: String, cumplido: Boolean) {
    val color = if (cumplido) Exito else MaterialTheme.colorScheme.error
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun RestablecerContrasenaPreview() {
    RinconSurAppTheme {
        RestablecerContrasenaContent(
            uiState = RestablecerContrasenaUiState(),
            onRutCambiado = {},
            onPreguntaSeleccionada = {},
            onRespuestaCambiada = {},
            onNuevaContrasenaCambiada = {},
            onConfirmarContrasenaCambiada = {},
            onAlternarVisibilidad = {},
            onRestablecer = {},
            onVolver = {},
            onDialogoCerrado = {}
        )
    }
}