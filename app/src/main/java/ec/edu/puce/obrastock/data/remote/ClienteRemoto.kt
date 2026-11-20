package ec.edu.puce.obrastock.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// Crea el cliente de Retrofit una sola vez, cuando se usa por primera vez.
object ClienteRemoto {

    private val json = Json { ignoreUnknownKeys = true }

    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(
            Interceptor { cadena ->
                val peticion = cadena.request().newBuilder()
                    .header("apikey", ConfiguracionRemota.CLAVE_ANONIMA)
                    .header("Authorization", "Bearer ${ConfiguracionRemota.CLAVE_ANONIMA}")
                    .build()
                cadena.proceed(peticion)
            }
        )
        .build()

    val api: ObraStockApi by lazy {
        Retrofit.Builder()
            .baseUrl(ConfiguracionRemota.URL_BASE)
            .client(clienteHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ObraStockApi::class.java)
    }
}
