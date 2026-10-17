package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.InicioSesionUiState
import cl.duoc.rinconsurapp.ui.components.BotonPrincipal
import cl.duoc.rinconsurapp.ui.components.CampoContrasena
import cl.duoc.rinconsurapp.ui.components.CampoTexto
import cl.duoc.rinconsurapp.ui.components.TarjetaSeccion
import cl.duoc.rinconsurapp.ui.theme.Exito
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.viewmodel.InicioSesionViewModel

@Composable
fun InicioSesionScreen(
    viewModel: InicioSesionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InicioSesionContent(
        uiState = uiState,
        alCambiarRut = { viewModel.onRutCambiado(it) },
        alCambiarContrasena = { viewModel.onContrasenaCambiada(it) },
        alAlternarVisibilidad = { viewModel.alternarVisibilidadContrasena() },
        alIniciarSesion = { viewModel.iniciarSesion() }
    )
}

@Composable
fun InicioSesionContent(
    uiState: InicioSesionUiState,
    alCambiarRut: (String) -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alAlternarVisibilidad: () -> Unit,
    alIniciarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TarjetaSeccion(titulo = stringResource(id = R.string.titulo_inicio_sesion)) {

            Spacer(modifier = Modifier.height(8.dp))

            CampoTexto(
                valor = uiState.rut,
                onValorCambia = alCambiarRut,
                etiqueta = stringResource(id = R.string.etiqueta_rut),
                icono = R.drawable.ic_persona,
                error = uiState.rutError
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = alCambiarContrasena,
                etiqueta = stringResource(id = R.string.etiqueta_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = alAlternarVisibilidad,
                error = uiState.contrasenaError
            )

            Spacer(modifier = Modifier.height(if (uiState.errorGeneral != null) 16.dp else 32.dp))

            if (uiState.errorGeneral != null) {
                Text(
                    text = uiState.errorGeneral,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            BotonPrincipal(
                texto = stringResource(id = R.string.btn_iniciar_sesion),
                onClick = alIniciarSesion,
                habilitado = uiState.esValido,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.autenticado) {
                Text(
                    text = stringResource(id = R.string.msg_acceso_concedido),
                    color = Exito,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InicioSesionPreview() {
    RinconSurAppTheme {
        InicioSesionContent(
            uiState = InicioSesionUiState(),
            alCambiarRut = {},
            alCambiarContrasena = {},
            alAlternarVisibilidad = {},
            alIniciarSesion = {}
        )
    }
}