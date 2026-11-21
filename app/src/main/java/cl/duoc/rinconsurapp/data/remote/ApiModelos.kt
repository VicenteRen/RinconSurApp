package cl.duoc.rinconsurapp.data.remote

import cl.duoc.rinconsurapp.model.Acceso
import cl.duoc.rinconsurapp.model.RegistroObservacion
import cl.duoc.rinconsurapp.model.Usuario



data class RegistroObservacionRequest(
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val tieneComprasExternas: Boolean,
    val accesoDestino: String?,
    val usuarioId: Long,
    val nombreObservador: String
)

data class RegistroObservacionResponse(
    val id: Long,
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val tieneComprasExternas: Boolean,
    val accesoDestino: String?,
    val fechaHora: String,
    val usuarioId: Long,
    val nombreObservador: String,
    val activo: Boolean
)

fun RegistroObservacionResponse.aRegistroObservacion() = RegistroObservacion(
    id = id,
    clasificacion = clasificacion,
    rangoEtario = rangoEtario,
    compania = compania,
    tieneComprasExternas = tieneComprasExternas,
    accesoDestino = accesoDestino,
    fechaHora = fechaHora,
    usuarioId = usuarioId,
    nombreObservador = nombreObservador
)



data class AccesoRequest(
    val codigo: String,
    val nombre: String
)

data class AccesoResponse(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val activo: Boolean
)

fun AccesoResponse.aAcceso() = Acceso(
    id = id,
    codigo = codigo,
    nombre = nombre,
    activo = activo
)



data class UsuarioRequest(
    val rut: String,
    val contrasena: String = "",
    val nombreCompleto: String,
    val correo: String,
    val telefono: String,
    val preguntaSeguridad: String = "¿Cuál es el nombre de tu primera mascota?",
    val respuestaSeguridad: String = "pendiente",
    val rolId: Long
)

data class UsuarioResponse(
    val id: Long,
    val rut: String,
    val contrasena: String,
    val nombreCompleto: String,
    val correo: String,
    val telefono: String,
    val rolId: Long,
    val rolNombre: String,
    val avatarUrl: String? = null,
    val primerIngreso: Boolean? = false,
    val activo: Boolean
)

fun UsuarioResponse.aUsuario() = Usuario(
    id = id,
    rut = rut,
    contrasena = contrasena,
    nombreCompleto = nombreCompleto,
    correo = correo,
    telefono = telefono,
    rolId = rolId,
    rolNombre = rolNombre,
    avatarUrl = RetrofitClient.resolverUrl(avatarUrl),
    primerIngreso = primerIngreso ?: false,
    activo = activo
)