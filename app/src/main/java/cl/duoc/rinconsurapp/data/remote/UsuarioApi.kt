package cl.duoc.rinconsurapp.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioApi {

    @GET("api/usuarios")
    suspend fun listarActivos(): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun buscarPorId(@Path("id") id: Long): UsuarioResponse

    @POST("api/usuarios")
    suspend fun guardar(@Body datos: UsuarioRequest): UsuarioResponse

    @POST("api/usuarios/login")
    suspend fun login(@Body credenciales: Map<String, String>): UsuarioResponse

    @PUT("api/usuarios/{id}/contacto")
    suspend fun actualizarPerfil(
        @Path("id") id: Long,
        @Body datos: Map<String, String>
    ): UsuarioResponse

    @PUT("api/usuarios/{id}/cambiar-contrasena")
    suspend fun cambiarContrasenaDesdePerfil(
        @Path("id") id: Long,
        @Body datos: Map<String, String>
    ): UsuarioResponse

    @PUT("api/usuarios/{id}/admin")
    suspend fun actualizarDesdeAdmin(
        @Path("id") id: Long,
        @Body datos: Map<String, String>
    ): UsuarioResponse

    @PUT("api/usuarios/{id}/primer-ingreso")
    suspend fun configurarPrimerIngreso(
        @Path("id") id: Long,
        @Body datos: Map<String, String>
    ): UsuarioResponse

    @PUT("api/usuarios/{id}/avatar")
    suspend fun actualizarAvatar(
        @Path("id") id: Long,
        @Body datos: Map<String, String>
    ): UsuarioResponse

    @POST("api/usuarios/verificar-identidad")
    suspend fun verificarIdentidad(@Body datos: Map<String, String>)

    @PUT("api/usuarios/recuperar-contrasena")
    suspend fun recuperarContrasena(@Body datos: Map<String, String>)

    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}