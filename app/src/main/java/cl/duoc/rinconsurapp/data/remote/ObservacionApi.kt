package cl.duoc.rinconsurapp.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ObservacionApi {

    @POST("api/observaciones")
    suspend fun guardar(@Body datos: RegistroObservacionRequest): RegistroObservacionResponse

    @GET("api/observaciones")
    suspend fun listarTodos(): List<RegistroObservacionResponse>

    @GET("api/observaciones/usuario/{usuarioId}")
    suspend fun listarPorUsuario(@Path("usuarioId") usuarioId: Long): List<RegistroObservacionResponse>

    @DELETE("api/observaciones/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}