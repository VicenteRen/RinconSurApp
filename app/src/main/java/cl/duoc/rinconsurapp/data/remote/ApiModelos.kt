package cl.duoc.rinconsurapp.data.remote

import cl.duoc.rinconsurapp.model.Acceso
import cl.duoc.rinconsurapp.model.RegistroObservacion

// =====================================================================
//  DTOs para ms-observaciones (Puerto 8081)
// =====================================================================

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

// =====================================================================
//  DTOs para ms-accesos (Puerto 8083)
// =====================================================================

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