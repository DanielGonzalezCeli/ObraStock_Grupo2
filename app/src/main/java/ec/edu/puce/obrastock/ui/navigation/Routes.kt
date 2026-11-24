package ec.edu.puce.obrastock.ui.navigation

import kotlinx.serialization.Serializable

// Cada destino es una clase u objeto serializable: los argumentos viajan con su tipo
// y el compilador avisa si una ruta está mal escrita.

@Serializable
object Bodega

@Serializable
data class RegistroMaterial(val materialId: Long? = null)

@Serializable
data class DetalleMaterial(val materialId: Long)
