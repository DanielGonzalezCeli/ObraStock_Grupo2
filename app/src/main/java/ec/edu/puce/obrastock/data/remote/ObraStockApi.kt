package ec.edu.puce.obrastock.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

// Servicio REST de Supabase. Para filtrar por id se envía "eq.5", por ejemplo.
interface ObraStockApi {

    @GET("materiales_referencia")
    suspend fun listarMaterialesReferencia(): List<MaterialReferenciaDto>

    @GET("movimientos")
    suspend fun listarMovimientos(): List<MovimientoDto>

    @Headers("Prefer: return=representation")
    @POST("movimientos")
    suspend fun crearMovimiento(@Body movimiento: MovimientoDto): List<MovimientoDto>

    @Headers("Prefer: return=representation")
    @PATCH("movimientos")
    suspend fun actualizarMovimiento(
        @Query("id") filtroId: String,
        @Body movimiento: MovimientoDto
    ): List<MovimientoDto>

    @DELETE("movimientos")
    suspend fun eliminarMovimiento(@Query("id") filtroId: String): Response<Unit>
}
