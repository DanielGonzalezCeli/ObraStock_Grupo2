package ec.edu.puce.obrastock.domain.model

/** Material guardado en la bodega de una obra. Inmutable: los cambios se hacen con copy(). */
data class Material(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val unidad: String,
    val stock: Double,
    val stockMinimo: Double,
    val obraId: Int,
) {
    /** Verdadero cuando el stock llegó al mínimo y hay que reponer. */
    val stockBajo: Boolean
        get() = stock <= stockMinimo
}
