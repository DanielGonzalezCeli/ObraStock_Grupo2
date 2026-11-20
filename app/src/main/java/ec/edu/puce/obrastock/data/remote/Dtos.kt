package ec.edu.puce.obrastock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Estructuras del JSON que viaja entre la aplicación y el servicio.

@Serializable
data class MaterialReferenciaDto(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val unidad: String,
    @SerialName("costo_referencia") val costoReferencia: Double
)

@Serializable
data class MovimientoDto(
    val id: Long? = null,
    @SerialName("material_id") val materialId: Long,
    val tipo: String,
    val cantidad: Double,
    @SerialName("costo_total") val costoTotal: Double? = null,
    val proveedor: String? = null,
    val fecha: Long
)
