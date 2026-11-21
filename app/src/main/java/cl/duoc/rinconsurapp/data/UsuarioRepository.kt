package cl.duoc.rinconsurapp.data

import cl.duoc.rinconsurapp.data.remote.RetrofitClient
import cl.duoc.rinconsurapp.data.remote.UsuarioApi
import cl.duoc.rinconsurapp.data.remote.UsuarioRequest
import cl.duoc.rinconsurapp.data.remote.aUsuario
import cl.duoc.rinconsurapp.model.Usuario
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class UsuarioRepository(
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi
) {

    suspend fun listarActivos(): List<Usuario> =
        llamar { usuarioApi.listarActivos() }.map { it.aUsuario() }

    suspend fun buscarPorId(id: Long): Usuario =
        llamar { usuarioApi.buscarPorId(id) }.aUsuario()

    suspend fun login(rut: String, contrasena: String): Usuario =
        llamar {
            usuarioApi.login(
                mapOf(
                    "rut" to rut.trim(),
                    "contrasena" to contrasena
                )
            )
        }.aUsuario()

    suspend fun crearUsuario(
        rut: String,
        contrasena: String = "",
        nombreCompleto: String,
        correo: String,
        telefono: String,
        preguntaSeguridad: String = "¿Cuál es el nombre de tu primera mascota?",
        respuestaSeguridad: String = "pendiente",
        rolId: Long
    ): Usuario {
        val request = UsuarioRequest(
            rut = rut.trim(),
            contrasena = contrasena,
            nombreCompleto = nombreCompleto.trim(),
            correo = correo.trim(),
            telefono = telefono.trim(),
            preguntaSeguridad = preguntaSeguridad,
            respuestaSeguridad = respuestaSeguridad.trim(),
            rolId = rolId
        )
        return llamar { usuarioApi.guardar(request) }.aUsuario()
    }

    suspend fun actualizarPerfil(id: Long, nombreCompleto: String, correo: String, telefono: String): Usuario =
        llamar {
            usuarioApi.actualizarPerfil(
                id = id,
                datos = mapOf(
                    "nombreCompleto" to nombreCompleto.trim(),
                    "correo" to correo.trim(),
                    "telefono" to telefono.trim()
                )
            )
        }.aUsuario()

    suspend fun cambiarContrasenaDesdePerfil(id: Long, contrasenaActual: String, nuevaContrasena: String): Usuario =
        llamar {
            usuarioApi.cambiarContrasenaDesdePerfil(
                id = id,
                datos = mapOf(
                    "contrasenaActual" to contrasenaActual,
                    "nuevaContrasena" to nuevaContrasena
                )
            )
        }.aUsuario()

    suspend fun actualizarDesdeAdmin(id: Long, correo: String, telefono: String, rolId: Long): Usuario =
        llamar {
            usuarioApi.actualizarDesdeAdmin(
                id = id,
                datos = mapOf(
                    "correo" to correo.trim(),
                    "telefono" to telefono.trim(),
                    "rolId" to rolId.toString()
                )
            )
        }.aUsuario()

    suspend fun configurarPrimerIngreso(
        id: Long,
        preguntaSeguridad: String,
        respuestaSeguridad: String,
        nuevaContrasena: String
    ): Usuario =
        llamar {
            usuarioApi.configurarPrimerIngreso(
                id = id,
                datos = mapOf(
                    "preguntaSeguridad" to preguntaSeguridad,
                    "respuestaSeguridad" to respuestaSeguridad.trim(),
                    "nuevaContrasena" to nuevaContrasena
                )
            )
        }.aUsuario()

    suspend fun actualizarAvatar(id: Long, avatarBytes: ByteArray): Usuario {
        val cuerpo = avatarBytes.toRequestBody("image/jpeg".toMediaType())
        val parte = MultipartBody.Part.createFormData("archivo", "avatar.jpg", cuerpo)
        return llamar {
            usuarioApi.actualizarAvatar(id = id, archivo = parte)
        }.aUsuario()
    }

    suspend fun verificarIdentidad(rut: String, preguntaSeguridad: String, respuestaSeguridad: String) =
        llamar {
            usuarioApi.verificarIdentidad(
                mapOf(
                    "rut" to rut.trim(),
                    "preguntaSeguridad" to preguntaSeguridad,
                    "respuestaSeguridad" to respuestaSeguridad.trim()
                )
            )
        }

    suspend fun recuperarContrasena(
        rut: String,
        preguntaSeguridad: String,
        respuestaSeguridad: String,
        nuevaContrasena: String
    ) = llamar {
        usuarioApi.recuperarContrasena(
            mapOf(
                "rut" to rut.trim(),
                "preguntaSeguridad" to preguntaSeguridad,
                "respuestaSeguridad" to respuestaSeguridad.trim(),
                "nuevaContrasena" to nuevaContrasena
            )
        )
    }

    suspend fun eliminarUsuario(id: Long) =
        llamar { usuarioApi.eliminar(id) }

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
        if (mensaje.isNotBlank()) return mensaje
        return when (e.code()) {
            401 -> "RUT o contraseña incorrecta. Vuelva a intentarlo."
            409 -> "El registro ya existe en el sistema."
            404 -> "Usuario no encontrado."
            else -> "Error del servidor (${e.code()})"
        }
    }
}