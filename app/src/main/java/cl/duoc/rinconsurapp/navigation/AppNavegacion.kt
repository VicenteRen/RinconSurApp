package cl.duoc.rinconsurapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.data.UsuarioRepository
import cl.duoc.rinconsurapp.model.Rol
import cl.duoc.rinconsurapp.model.Usuario
import cl.duoc.rinconsurapp.ui.screens.ConfigurarSeguridadScreen
import cl.duoc.rinconsurapp.ui.screens.CrearContrasenaScreen
import cl.duoc.rinconsurapp.ui.screens.CrearUsuarioScreen
import cl.duoc.rinconsurapp.ui.screens.DashboardAdminScreen
import cl.duoc.rinconsurapp.ui.screens.DashboardObservadorScreen
import cl.duoc.rinconsurapp.ui.screens.GestionAccesosScreen
import cl.duoc.rinconsurapp.ui.screens.GestionUsuariosScreen
import cl.duoc.rinconsurapp.ui.screens.HistorialAdminScreen
import cl.duoc.rinconsurapp.ui.screens.HistorialObservadorScreen
import cl.duoc.rinconsurapp.ui.screens.InicioSesionScreen
import cl.duoc.rinconsurapp.ui.screens.PerfilUsuarioScreen
import cl.duoc.rinconsurapp.ui.screens.RegistroObservacionScreen
import cl.duoc.rinconsurapp.ui.screens.RestablecerContrasenaScreen
import cl.duoc.rinconsurapp.ui.theme.Dimens
import cl.duoc.rinconsurapp.viewmodel.PrimerIngresoViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repositorio = remember { UsuarioRepository() }
    val sesion = remember { SesionPreferencias(context) }
    val primerIngresoViewModel: PrimerIngresoViewModel = viewModel()

    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var sesionVerificada by remember { mutableStateOf(false) }
    var enProcesoPrimerIngreso by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val idGuardado = sesion.leer()
        if (idGuardado != SesionPreferencias.SIN_SESION) {
            try {
                val usuario = repositorio.buscarPorId(idGuardado)
                if (usuario.primerIngreso && !enProcesoPrimerIngreso) {
                    sesion.borrar()
                    usuarioActual = null
                } else {
                    usuarioActual = usuario
                }
            } catch (e: Exception) {
                sesion.borrar()
                usuarioActual = null
            }
        }
        sesionVerificada = true
    }

    if (!sesionVerificada) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val destinoInicial = when {
        usuarioActual == null -> Ruta.InicioSesion.ruta
        usuarioActual!!.primerIngreso -> Ruta.ConfigurarSeguridad.ruta
        usuarioActual!!.rolId == Rol.ID_ADMIN -> Ruta.DashboardAdmin.ruta
        else -> Ruta.RegistroObservacion.ruta
    }

    val ejecutarCierreSesion = {
        sesion.borrar()
        enProcesoPrimerIngreso = false
        usuarioActual = null
        primerIngresoViewModel.limpiarFormulario()
        navController.navigate(Ruta.InicioSesion.ruta) {
            popUpTo(0) { inclusive = true }
        }
    }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = { Text(stringResource(R.string.dialog_cerrar_sesion_titulo)) },
            text = { Text(stringResource(R.string.dialog_cerrar_sesion_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        ejecutarCierreSesion()
                    }
                ) {
                    Text(stringResource(R.string.btn_cerrar_sesion))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = rutaActual != Ruta.InicioSesion.ruta &&
                rutaActual != Ruta.ConfigurarSeguridad.ruta &&
                rutaActual != Ruta.CrearContrasena.ruta,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = if (usuarioActual != null) {
                        stringResource(
                            R.string.saludo_menu,
                            usuarioActual!!.nombreCompleto,
                            usuarioActual!!.rolNombre
                        )
                    } else {
                        stringResource(R.string.app_name)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()

                if (usuarioActual?.rolId == Rol.ID_OBSERVADOR) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.titulo_observador)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_nuevo_registro),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.RegistroObservacion.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.RegistroObservacion.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_mi_dashboard)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_dashboard),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.DashboardObservador.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.DashboardObservador.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_mi_historial)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_historial),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.HistorialObservador.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.HistorialObservador.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                if (usuarioActual?.rolId == Rol.ID_ADMIN) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_dashboard_global)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_dashboard),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.DashboardAdmin.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.DashboardAdmin.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_historial_global)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_historial),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.HistorialAdmin.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.HistorialAdmin.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_gestion_accesos)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_accesos),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.GestionAccesos.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.GestionAccesos.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_crear_usuario)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_agregar_usuario),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.CrearUsuario.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.CrearUsuario.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_gestion_usuarios)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_ver_usuarios),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.GestionUsuarios.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.GestionUsuarios.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                if (usuarioActual != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.espacioChico))
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.titulo_mi_perfil)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_solo),
                                contentDescription = null
                            )
                        },
                        selected = rutaActual == Ruta.Perfil.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.Perfil.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.btn_cerrar_sesion)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_salir),
                                contentDescription = null
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mostrarDialogoCerrarSesion = true
                        }
                    )
                }
            }
        }
    ) {
        val abrirMenu: () -> Unit = { scope.launch { drawerState.open() } }

        NavHost(
            navController = navController,
            startDestination = destinoInicial
        ) {
            composable(Ruta.InicioSesion.ruta) {
                InicioSesionScreen(
                    onLoginExitoso = {
                        scope.launch {
                            val idUsuario = sesion.leer()
                            if (idUsuario != SesionPreferencias.SIN_SESION) {
                                try {
                                    val usuarioLogueado = repositorio.buscarPorId(idUsuario)
                                    usuarioActual = usuarioLogueado
                                    if (usuarioLogueado.primerIngreso) {
                                        enProcesoPrimerIngreso = true
                                        primerIngresoViewModel.limpiarFormulario()
                                    } else {
                                        enProcesoPrimerIngreso = false
                                    }
                                    val rutaDestino = when {
                                        usuarioLogueado.primerIngreso -> Ruta.ConfigurarSeguridad.ruta
                                        usuarioLogueado.rolId == Rol.ID_ADMIN -> Ruta.DashboardAdmin.ruta
                                        else -> Ruta.RegistroObservacion.ruta
                                    }
                                    navController.navigate(rutaDestino) {
                                        popUpTo(Ruta.InicioSesion.ruta) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        }
                    },
                    onOlvideContrasena = {
                        navController.navigate(Ruta.RestablecerContrasena.ruta)
                    }
                )
            }
            composable(Ruta.ConfigurarSeguridad.ruta) {
                ConfigurarSeguridadScreen(
                    viewModel = primerIngresoViewModel,
                    onContinuar = {
                        navController.navigate(Ruta.CrearContrasena.ruta)
                    }
                )
            }
            composable(Ruta.CrearContrasena.ruta) {
                CrearContrasenaScreen(
                    viewModel = primerIngresoViewModel,
                    onPrimerIngresoCompletado = {
                        enProcesoPrimerIngreso = false
                        usuarioActual = usuarioActual?.copy(primerIngreso = false)
                        val destinoFinal = if (usuarioActual?.rolId == Rol.ID_ADMIN) {
                            Ruta.DashboardAdmin.ruta
                        } else {
                            Ruta.RegistroObservacion.ruta
                        }
                        navController.navigate(destinoFinal) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Ruta.RegistroObservacion.ruta) {
                RegistroObservacionScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.DashboardObservador.ruta) {
                DashboardObservadorScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.HistorialObservador.ruta) {
                HistorialObservadorScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.DashboardAdmin.ruta) {
                DashboardAdminScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.HistorialAdmin.ruta) {
                HistorialAdminScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.GestionAccesos.ruta) {
                GestionAccesosScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.Perfil.ruta) {
                PerfilUsuarioScreen(
                    onAbrirMenu = abrirMenu,
                    onCerrarSesion = ejecutarCierreSesion,
                    onNombreActualizado = { nuevoNombre ->
                        usuarioActual = usuarioActual?.copy(nombreCompleto = nuevoNombre)
                    }
                )
            }
            composable(Ruta.RestablecerContrasena.ruta) {
                RestablecerContrasenaScreen(
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Ruta.CrearUsuario.ruta) {
                CrearUsuarioScreen(
                    onAbrirMenu = abrirMenu,
                    onUsuarioCreado = {
                        navController.navigate(Ruta.GestionUsuarios.ruta) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Ruta.GestionUsuarios.ruta) {
                GestionUsuariosScreen(
                    onAbrirMenu = abrirMenu
                )
            }
        }
    }
}