package cl.duoc.rinconsurapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.Rol
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

    // Lee si ya había una sesión guardada en SharedPreferences al abrir la app
    var usuarioActual by remember {
        mutableStateOf(db.buscarUsuarioPorId(sesion.leer()))
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    // Saber en qué pantalla estamos para marcarla activa en el menú lateral
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // Si había sesión activa al abrir la app, entra directo a su pantalla según rol
    val destinoInicial = when (usuarioActual?.rolId) {
        Rol.ID_ADMIN -> Ruta.DashboardAdmin.ruta
        Rol.ID_OBSERVADOR -> Ruta.RegistroObservacion.ruta
        else -> Ruta.InicioSesion.ruta
    }

    // Función para cerrar sesión y volver al Login limpiando el historial
    val ejecutarCierreSesion = {
        sesion.borrar()
        usuarioActual = null
        navController.navigate(Ruta.InicioSesion.ruta) {
            popUpTo(0) { inclusive = true }
        }
    }

    // AlertDialog del profesor para confirmar cierre de sesión
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar tu sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        ejecutarCierreSesion()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Deshabilita el deslizamiento del menú cuando estamos en la pantalla de Login
        gesturesEnabled = rutaActual != Ruta.InicioSesion.ruta,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = if (usuarioActual != null) {
                        "Hola, ${usuarioActual!!.nombreCompleto}\n(${usuarioActual!!.rolNombre})"
                    } else {
                        "Rincón Sur"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()

                // --- OPCIONES EXCLUSIVAS DEL OBSERVADOR ---
                if (usuarioActual?.rolId == Rol.ID_OBSERVADOR) {
                    NavigationDrawerItem(
                        label = { Text("Nuevo Registro") },
                        selected = rutaActual == Ruta.RegistroObservacion.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.RegistroObservacion.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Mi Dashboard") },
                        selected = rutaActual == Ruta.DashboardObservador.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.DashboardObservador.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Mi Historial") },
                        selected = rutaActual == Ruta.HistorialObservador.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.HistorialObservador.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // --- OPCIONES EXCLUSIVAS DEL ADMINISTRADOR ---
                if (usuarioActual?.rolId == Rol.ID_ADMIN) {
                    NavigationDrawerItem(
                        label = { Text("Dashboard Global") },
                        selected = rutaActual == Ruta.DashboardAdmin.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.DashboardAdmin.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Historial Global") },
                        selected = rutaActual == Ruta.HistorialAdmin.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.HistorialAdmin.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // --- OPCIONES COMUNES (PERFIL Y CERRAR SESIÓN) ---
                if (usuarioActual != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.espacioChico))
                    NavigationDrawerItem(
                        label = { Text("Mi Perfil") },
                        selected = rutaActual == Ruta.Perfil.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Ruta.Perfil.ruta) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Cerrar Sesión") },
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
            // 1. Pantalla de Login
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

            // 2. Pantallas del Observador
            composable(Ruta.RegistroObservacion.ruta) {
                RegistroObservacionScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.DashboardObservador.ruta) {
                DashboardObservadorScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.HistorialObservador.ruta) {
                HistorialObservadorScreen(onAbrirMenu = abrirMenu)
            }

            // 3. Pantallas del Administrador
            composable(Ruta.DashboardAdmin.ruta) {
                DashboardAdminScreen(onAbrirMenu = abrirMenu)
            }
            composable(Ruta.HistorialAdmin.ruta) {
                HistorialAdminScreen(onAbrirMenu = abrirMenu)
            }

            // 4. Pantalla de Perfil (Común para ambos roles)
            composable(Ruta.Perfil.ruta) {
                PerfilUsuarioScreen(
                    onVolver = { navController.navigateUp() },
                    onCerrarSesion = ejecutarCierreSesion
                )
            }

            // 5. Pantalla del formulario de restablecer contraseña
            composable(Ruta.RestablecerContrasena.ruta) {
                RestablecerContrasenaScreen(
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}