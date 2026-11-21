package cl.duoc.rinconsurapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // private const val URL_GATEWAY = "http://10.0.2.2:8080/"
    private const val URL_GATEWAY = "http://127.0.0.1:8080/"

    fun resolverUrl(ruta: String?): String? {
        if (ruta.isNullOrBlank()) return null
        if (ruta.startsWith("http://") || ruta.startsWith("https://")) return ruta
        return URL_GATEWAY.trimEnd('/') + "/" + ruta.trimStart('/')
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_GATEWAY)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val observacionApi: ObservacionApi by lazy {
        retrofit.create(ObservacionApi::class.java)
    }

    val accesoApi: AccesoApi by lazy {
        retrofit.create(AccesoApi::class.java)
    }

    val usuarioApi: UsuarioApi by lazy {
        retrofit.create(UsuarioApi::class.java)
    }
}