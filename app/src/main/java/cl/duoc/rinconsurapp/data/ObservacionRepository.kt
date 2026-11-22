package cl.duoc.rinconsurapp.data

import cl.duoc.rinconsurapp.data.remote.AccesoApi
import cl.duoc.rinconsurapp.data.remote.AccesoRequest
import cl.duoc.rinconsurapp.data.remote.ObservacionApi
import cl.duoc.rinconsurapp.data.remote.RegistroObservacionRequest
import cl.duoc.rinconsurapp.data.remote.RetrofitClient
import cl.duoc.rinconsurapp.data.remote.aAcceso
import cl.duoc.rinconsurapp.data.remote.aRegistroObservacion
import cl.duoc.rinconsurapp.model.Acceso
import cl.duoc.rinconsurapp.model.RegistroObservacion
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

import cl.duoc.rinconsurapp.data.remote.SolicitudAnulacionRequest

class ObservacionRepository(
    private val observacionApi: ObservacionApi = RetrofitClient.observacionApi,
    private val accesoApi: AccesoApi = RetrofitClient.accesoApi
) {

    suspend fun guardarObservacion(registro: RegistroObservacion): RegistroObservacion {
        val request = RegistroObservacionRequest(
            clasificacion = registro.clasificacion,
            rangoEtario = registro.rangoEtario,
            compania = registro.compania,
            tieneComprasExternas = registro.tieneComprasExternas,
            accesoDestino = registro.accesoDestino,
            usuarioId = registro.usuarioId,
            nombreObservador = registro.nombreObservador
        )
        return llamar { observacionApi.guardar(request) }.aRegistroObservacion()
    }

    suspend fun listarTodas(): List<RegistroObservacion> =
        llamar { observacionApi.listarTodos() }.map { it.aRegistroObservacion() }

    suspend fun listarPorUsuario(usuarioId: Long): List<RegistroObservacion> =
        llamar { observacionApi.listarPorUsuario(usuarioId) }.map { it.aRegistroObservacion() }


    suspend fun anularObservacion(
        id: Long,
        rut: String,
        contrasena: String,
        motivo: String
    ) {
        val motivoValidado = motivo.trim()

        require(motivoValidado.length in 10..255) {
            "El motivo debe tener entre 10 y 255 caracteres."
        }

        require(motivoValidado.any { it.isLetter() }) {
            "El motivo debe contener letras."
        }

        require(motivoValidado.none { it.isISOControl() }) {
            "El motivo contiene caracteres no permitidos."
        }

        require(contrasena.isNotBlank()) {
            "Debes ingresar tu contraseña actual."
        }

        llamar {
            observacionApi.anular(
                id,
                SolicitudAnulacionRequest(
                    rut = rut.trim(),
                    contrasena = contrasena,
                    motivo = motivoValidado
                )
            )
        }
    }


    // --- Operaciones de Accesos ---
    suspend fun listarAccesosActivos(): List<Acceso> =
        llamar { accesoApi.listarActivos() }.map { it.aAcceso() }

    suspend fun listarTodosLosAccesos(): List<Acceso> =
        llamar { accesoApi.listarTodos() }.map { it.aAcceso() }

    suspend fun crearAcceso(codigo: String, nombre: String): Acceso =
        llamar { accesoApi.guardar(AccesoRequest(codigo, nombre)) }.aAcceso()

    suspend fun cambiarEstadoAcceso(id: Long): Acceso =
        llamar { accesoApi.cambiarEstado(id) }.aAcceso()

    // --- Manejo de errores de red del profe ---
    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        peticion()
    } catch (e: HttpException) {
        throw Exception(mensajeDelServidor(e))
    } catch (e: IOException) {
        throw Exception("Error de red: ${e.javaClass.simpleName} - ${e.message}")
    }

    private fun mensajeDelServidor(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string()
        val mensaje = try {
            val json = JSONObject(cuerpo ?: "")
            json.optString("message").ifBlank { json.optString("detail") }
        } catch (ex: Exception) {
            ""
        }
        return mensaje.ifBlank {
            when (e.code()) {
                400 -> "Verifica los datos ingresados."
                401 -> "Debes iniciar sesión nuevamente."
                403 -> "Contraseña incorrecta o no tienes permisos para anular."
                404 -> "La observación no fue encontrada."
                409 -> "Esta observación ya fue anulada."
                else -> "No se pudo completar la operación. Inténtalo nuevamente."
            }
        }
    }
}