package ec.edu.puce.obrastock.domain.model

data class Material(
    val id: Long,
    val obraId: Long,
    val nombre: String,
    val categoria: String,
    val unidad: String,
    val existencias: Double,
    val existenciasMinimas: Double,
    val costoUnitario: Double,
    val descripcion: String? = null
)

// Se calcula al vuelo: no se guarda en la base de datos.
val Material.existenciasBajas: Boolean
    get() = existencias <= existenciasMinimas
