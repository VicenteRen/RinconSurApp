package cl.duoc.rinconsurapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface ObservacionApi {

    @POST("api/observaciones")
    suspend fun guardar(@Body datos: RegistroObservacionRequest): RegistroObservacionResponse

    @GET("api/observaciones")
    suspend fun listarTodos(): List<RegistroObservacionResponse>

    @GET("api/observaciones/usuario/{usuarioId}")
    suspend fun listarPorUsuario(@Path("usuarioId") usuarioId: Long): List<RegistroObservacionResponse>

    @PUT("api/observaciones/{id}/anular")
    suspend fun anular(
        @Path("id") id: Long,
        @Body datos: SolicitudAnulacionRequest
    )
}

data class SolicitudAnulacionRequest(
    val rut: String,
    val contrasena: String,
    val motivo: String
)