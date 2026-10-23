package cl.duoc.rinconsurapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.model.*
import cl.duoc.rinconsurapp.ui.components.*
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme
import cl.duoc.rinconsurapp.viewmodel.RegistroObservacionViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import cl.duoc.rinconsurapp.data.Vibracion

@Composable
fun RegistroObservacionScreen(
    onAbrirMenu: () -> Unit = {},
    viewModel: RegistroObservacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val mensajeExito = stringResource(R.string.msg_registro_exitoso)

    RegistroObservacionContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAbrirMenu = onAbrirMenu,
        onAccesoDestinoSeleccionado = viewModel::onAccesoDestinoSeleccionado,
        onClasificacionSeleccionada = viewModel::onClasificacionSeleccionada,
        onTieneComprasCambia = viewModel::onTieneComprasCambia,
        onCompaniaSeleccionada = viewModel::onCompaniaSeleccionada,
        onRangoEtarioSeleccionado = viewModel::onRangoEtarioSeleccionado,
        onLimpiar = viewModel::onLimpiar,
        onRegistrar = {
            val guardado = viewModel.onRegistrar()

            if (guardado) {
                Vibracion.vibrar(context)

                scope.launch {
                    snackbarHostState.showSnackbar(mensajeExito)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroObservacionContent(
    uiState: RegistroObservacionUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAbrirMenu: () -> Unit = {},
    onAccesoDestinoSeleccionado: (Acceso) -> Unit,
    onClasificacionSeleccionada: (Clasificacion) -> Unit,
    onTieneComprasCambia: (Boolean) -> Unit,
    onCompaniaSeleccionada: (Compania) -> Unit,
    onRangoEtarioSeleccionado: (RangoEtario) -> Unit,
    onLimpiar: () -> Unit,
    onRegistrar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_observador)) },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Abrir menú"
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 01. Punto de Acceso en una Row con insignias de colores
            TarjetaSeccion(titulo = stringResource(R.string.seccion_acceso)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Acceso.entries.forEach { acceso ->
                        val seleccionado = (acceso == uiState.accesoDestino)

                        // Color distintivo para cada acceso
                        val colorAcceso = when (acceso) {
                            Acceso.A1 -> Color(0xFF5E35B1) // Morado
                            Acceso.A2 -> Color(0xFF00897B) // Turquesa
                            Acceso.A3 -> Color(0xFFFB8C00) // Naranja
                            Acceso.A4 -> Color(0xFFE53935) // Rojo
                        }

                        // Extraemos el código ("A1") y el nombre corto ("Principal")
                        val codigo = acceso.name // "A1", "A2", "A3", "A4"
                        val nombreCorto = acceso.etiqueta.substringAfter("- ").replace("Acceso ", "")

                        OutlinedCard(
                            onClick = { onAccesoDestinoSeleccionado(acceso) },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (seleccionado) colorAcceso.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = BorderStroke(
                                width = if (seleccionado) 2.dp else 1.dp,
                                color = if (seleccionado) colorAcceso else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Círculo de color con "A1", "A2", etc.
                                Surface(
                                    shape = CircleShape,
                                    color = if (seleccionado) colorAcceso else colorAcceso.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = codigo,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (seleccionado) Color.White else colorAcceso
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = nombreCorto,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 02. Clasificación
            TarjetaSeccion(titulo = stringResource(R.string.seccion_clasificacion)) {
                GrupoRadio(
                    opciones = Clasificacion.entries,
                    seleccionada = uiState.clasificacion,
                    onSeleccionar = onClasificacionSeleccionada,
                    textoDe = { it.etiqueta }
                )
            }

            // 03. Variables Adicionales (¿Trae compras?)
            TarjetaSeccion(titulo = stringResource(R.string.seccion_variables)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.pregunta_compras),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.tieneComprasExternas,
                        onCheckedChange = onTieneComprasCambia
                    )
                }
            }

            // 04. Compañía con iconos visuales
            TarjetaSeccion(titulo = stringResource(R.string.seccion_compania)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Compania.entries.forEach { compania ->
                        val seleccionado = (compania == uiState.compania)

                        val iconoRes = when (compania) {
                            Compania.SOLO -> R.drawable.ic_solo
                            Compania.PAREJA -> R.drawable.ic_pareja
                            Compania.CON_NINOS -> R.drawable.ic_ninos
                            Compania.GRUPO -> R.drawable.ic_grupo
                        }

                        val etiquetaCorta = when (compania) {
                            Compania.SOLO -> "Solo"
                            Compania.PAREJA -> "Pareja"
                            Compania.CON_NINOS -> "Niños"
                            Compania.GRUPO -> "Grupo"
                        }

                        OutlinedCard(
                            onClick = { onCompaniaSeleccionada(compania) },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (seleccionado) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                }
                            ),
                            border = BorderStroke(
                                width = if (seleccionado) 2.dp else 1.dp,
                                color = if (seleccionado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(iconoRes),
                                    contentDescription = compania.etiqueta,
                                    tint = if (seleccionado) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = etiquetaCorta,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                    color = if (seleccionado) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 05. Rango Etario
            TarjetaSeccion(titulo = stringResource(R.string.seccion_rango_etario)) {
                GrupoRadio(
                    opciones = RangoEtario.entries,
                    seleccionada = uiState.rangoEtario,
                    onSeleccionar = onRangoEtarioSeleccionado,
                    textoDe = { it.etiqueta }
                )
            }

            // Botones Limpiar y Guardar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_limpiar),
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_guardar),
                    onClick = onRegistrar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun RegistroObservacionPreview() {
    RinconSurAppTheme {
        RegistroObservacionContent(
            uiState = RegistroObservacionUiState(nombreObservador = "Vicente Ponce"),
            onAccesoDestinoSeleccionado = {},
            onClasificacionSeleccionada = {},
            onTieneComprasCambia = {},
            onCompaniaSeleccionada = {},
            onRangoEtarioSeleccionado = {},
            onLimpiar = {},
            onRegistrar = {}
        )
    }
}