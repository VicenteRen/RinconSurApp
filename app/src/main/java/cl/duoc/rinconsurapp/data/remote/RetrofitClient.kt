package cl.duoc.rinconsurapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


//    private const val URL_OBSERVACIONES = "http://10.0.2.2:8081/"
//    private const val URL_USUARIOS = "http://10.0.2.2:8082/"
//    private const val URL_ACCESOS = "http://10.0.2.2:8083/"

    private const val URL_OBSERVACIONES = "http://127.0.0.1:8081/"
    private const val URL_USUARIOS = "http://127.0.0.1:8082/"
    private const val URL_ACCESOS = "http://127.0.0.1:8083/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val observacionApi: ObservacionApi by lazy {
        crear(URL_OBSERVACIONES).create(ObservacionApi::class.java)
    }

    val accesoApi: AccesoApi by lazy {
        crear(URL_ACCESOS).create(AccesoApi::class.java)
    }
}