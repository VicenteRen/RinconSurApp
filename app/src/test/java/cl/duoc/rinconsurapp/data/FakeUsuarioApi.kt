package cl.duoc.rinconsurapp.data

import cl.duoc.rinconsurapp.data.remote.UsuarioApi
import cl.duoc.rinconsurapp.data.remote.UsuarioRequest
import cl.duoc.rinconsurapp.data.remote.UsuarioResponse
import okhttp3.MultipartBody
import java.io.IOException

class FakeUsuarioApi : UsuarioApi {

    private val usuarios = listOf(
        UsuarioResponse(
            id = 1L,
            rut = "214297353",
            contrasena = "hash",
            nombreCompleto = "Vicente Ponce",
            correo = "v.ponce@rinconsur.cl",
            telefono = "987654321",
            rolId = 1L,
            rolNombre = "Observador",
            avatarUrl = "/api/usuarios/avatares/usuario_1.jpg",
            primerIngreso = false,
            activo = true
        ),
        UsuarioResponse(
            id = 2L,
            rut = "228405566",
            contrasena = "hash",
            nombreCompleto = "Jefe Administrador",
            correo = "admin@rinconsur.cl",
            telefono = "911223344",
            rolId = 2L,
            rolNombre = "Administrador",
            avatarUrl = null,
            primerIngreso = false,
            activo = true
        )
    )

    var sinConexion = false
    var ultimoRutLogin: String? = null
    var recibioArchivoAvatar = false

    override suspend fun listarActivos(): List<UsuarioResponse> {
        if (sinConexion) throw IOException("Servidor apagado")
        return usuarios
    }

    override suspend fun buscarPorId(id: Long): UsuarioResponse {
        if (sinConexion) throw IOException("Servidor apagado")
        return usuarios.first { it.id == id }
    }

    override suspend fun guardar(datos: UsuarioRequest): UsuarioResponse {
        if (sinConexion) throw IOException("Servidor apagado")
        return UsuarioResponse(
            id = 3L,
            rut = datos.rut,
            contrasena = "hash",
            nombreCompleto = datos.nombreCompleto,
            correo = datos.correo,
            telefono = datos.telefono,
            rolId = datos.rolId,
            rolNombre = if (datos.rolId == 2L) "Administrador" else "Observador",
            avatarUrl = null,
            primerIngreso = true,
            activo = true
        )
    }

    override suspend fun login(credenciales: Map<String, String>): UsuarioResponse {
        if (sinConexion) throw IOException("Servidor apagado")
        ultimoRutLogin = credenciales["rut"]
        return usuarios.first { it.rut == credenciales["rut"] }
    }

    override suspend fun actualizarAvatar(id: Long, archivo: MultipartBody.Part): UsuarioResponse {
        if (sinConexion) throw IOException("Servidor apagado")
        recibioArchivoAvatar = true
        return usuarios.first { it.id == id }.copy(
            avatarUrl = "/api/usuarios/avatares/usuario_${id}_nuevo.jpg"
        )
    }

    override suspend fun actualizarPerfil(id: Long, datos: Map<String, String>): UsuarioResponse = TODO()
    override suspend fun cambiarContrasenaDesdePerfil(id: Long, datos: Map<String, String>): UsuarioResponse = TODO()
    override suspend fun actualizarDesdeAdmin(id: Long, datos: Map<String, String>): UsuarioResponse = TODO()
    override suspend fun configurarPrimerIngreso(id: Long, datos: Map<String, String>): UsuarioResponse = TODO()
    override suspend fun verificarIdentidad(datos: Map<String, String>) = TODO()
    override suspend fun recuperarContrasena(datos: Map<String, String>) = TODO()
    override suspend fun eliminar(id: Long) = TODO()
}