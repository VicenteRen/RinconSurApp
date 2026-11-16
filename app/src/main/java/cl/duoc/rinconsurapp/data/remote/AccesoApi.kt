package cl.duoc.rinconsurapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AccesoApi {

    @GET("api/accesos")
    suspend fun listarTodos(): List<AccesoResponse>

    @GET("api/accesos/activos")
    suspend fun listarActivos(): List<AccesoResponse>

    @POST("api/accesos")
    suspend fun guardar(@Body datos: AccesoRequest): AccesoResponse

    @PUT("api/accesos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long): AccesoResponse
}