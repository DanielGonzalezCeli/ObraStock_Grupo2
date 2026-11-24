package ec.edu.puce.obrastock.domain.model

enum class TipoMovimiento { COMPRA, CONSUMO }

data class Movimiento(
    val id: Long,
    val materialId: Long,
    val tipo: TipoMovimiento,
    val cantidad: Double,
    val costoTotal: Double?,
    val proveedor: String?,
    val fecha: Long
)
