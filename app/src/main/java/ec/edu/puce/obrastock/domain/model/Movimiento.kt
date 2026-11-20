package ec.edu.puce.obrastock.domain.model

/** Entrada (llega material) o salida (se usa en la obra). */
enum class TipoMovimiento { ENTRADA, SALIDA }

/** Registro de un cambio en el stock de un material. */
data class Movimiento(
    val id: Int,
    val materialId: Int,
    val tipo: TipoMovimiento,
    val cantidad: Double,
    val fechaMillis: Long,
    val nota: String = "",
)
