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

class ObservacionRepository(
    private val observacionApi: ObservacionApi = RetrofitClient.observacionApi,
    private val accesoApi: AccesoApi = RetrofitClient.accesoApi
) {

    // --- Operaciones de Observaciones ---
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

    suspend fun eliminarObservacion(id: Long) =
        llamar { observacionApi.eliminar(id) }

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
            JSONObject(cuerpo ?: "").optString("message")
        } catch (ex: Exception) {
            ""
        }
        return mensaje.ifBlank { "Error del servidor (${e.code()})" }
    }
}