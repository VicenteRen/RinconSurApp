package cl.duoc.rinconsurapp.navigation

sealed class Ruta(val ruta: String) {
    data object InicioSesion : Ruta("inicio_sesion")
    data object Perfil : Ruta("perfil")
    data object RestablecerContrasena : Ruta("restablecer_contrasena")
    data object RegistroObservacion : Ruta("registro_observacion")
    data object DashboardObservador : Ruta("dashboard_observador")
    data object HistorialObservador : Ruta("historial_observador")
    data object DashboardAdmin : Ruta("dashboard_admin")
    data object HistorialAdmin : Ruta("historial_admin")
    data object CrearUsuario : Ruta("crear_usuario")
    data object GestionUsuarios : Ruta("gestion_usuarios")
}