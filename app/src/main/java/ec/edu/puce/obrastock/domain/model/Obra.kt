package ec.edu.puce.obrastock.domain.model

/** Una obra de construcción; cada material de la bodega pertenece a una obra. */
data class Obra(
    val id: Int,
    val nombre: String,
    val direccion: String,
)
