package cl.duoc.rinconsurapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.rinconsurapp.R
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.Rol
import cl.duoc.rinconsurapp.ui.screens.CrearUsuarioScreen
import cl.duoc.rinconsurapp.ui.screens.DashboardAdminScreen
import cl.duoc.rinconsurapp.ui.screens.DashboardObservadorScreen
import cl.duoc.rinconsurapp.ui.screens.HistorialAdminScreen
import cl.duoc.rinconsurapp.ui.screens.HistorialObservadorScreen
import cl.duoc.rinconsurapp.ui.screens.InicioSesionScreen
import cl.duoc.rinconsurapp.ui.screens.PerfilUsuarioScreen
import cl.duoc.rinconsurapp.ui.screens.RegistroObservacionScreen
import cl.duoc.rinconsurapp.ui.screens.RestablecerContrasenaScreen
import cl.duoc.rinconsurapp.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { RinconSurDbHelper(context) }
    val sesion = remember { SesionPreferencias(context) }

    DisposableEffect(Unit) {
        onDispose { db.close() }
    }

    var usuarioActual by remember {
        mutableStateOf(db.buscarUsuarioPorId(sesion.leer()))
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val destinoInicial = when (usuarioActual?.rolId) {
        Rol.ID_ADMIN -> Ruta.DashboardAdmin.ruta
        Rol.ID_OBSERVADOR -> Ruta.RegistroObservacion.ruta
        else -> Ruta.InicioSesion.ruta
    }

    val ejecutarCierreSesion = {
        sesion.borrar()
        usuarioActual = null
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
        gesturesEnabled = rutaActual != Ruta.InicioSesion.ruta,
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
                        val usuarioLogueado = db.buscarUsuarioPorId(sesion.leer())
                        usuarioActual = usuarioLogueado
                        val rutaDestino = if (usuarioLogueado?.rolId == Rol.ID_ADMIN) {
                            Ruta.DashboardAdmin.ruta
                        } else {
                            Ruta.RegistroObservacion.ruta
                        }
                        navController.navigate(rutaDestino) {
                            popUpTo(Ruta.InicioSesion.ruta) { inclusive = true }
                        }
                    },
                    onOlvideContrasena = {
                        navController.navigate(Ruta.RestablecerContrasena.ruta)
                    }
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
            composable(Ruta.Perfil.ruta) {
                PerfilUsuarioScreen(
                    onVolver = { navController.navigateUp() },
                    onCerrarSesion = ejecutarCierreSesion
                )
            }
            composable(Ruta.RestablecerContrasena.ruta) {
                RestablecerContrasenaScreen(
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Ruta.CrearUsuario.ruta) {
                CrearUsuarioScreen(
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Ruta.GestionUsuarios.ruta) {
                // GestionUsuariosScreen(onVolver = { navController.popBackStack() })
            }
        }
    }
}