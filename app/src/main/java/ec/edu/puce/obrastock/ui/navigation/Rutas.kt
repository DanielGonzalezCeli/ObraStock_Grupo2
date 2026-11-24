package ec.edu.puce.obrastock.ui.navigation

import kotlinx.serialization.Serializable

/*
 * Grafo de navegación tipada: cada destino es un tipo serializable.
 * El compilador verifica los argumentos; no hay rutas en texto como "detalle/{id}".
 */

@Serializable
data object BodegaRoute

/**
 * Registro de compras y consumos. Los argumentos tienen valores por defecto para abrirlo
 * vacío desde la barra inferior o precargado desde el detalle de un material.
 */
@Serializable
data class RegistroRoute(
    val materialId: Long = SIN_MATERIAL,
    val tipo: String = "COMPRA"
) {
    companion object {
        const val SIN_MATERIAL = -1L
    }
}

@Serializable
data object ResumenRoute

@Serializable
data object HistorialRoute

@Serializable
data class DetalleMaterialRoute(
    val obraId: Long,
    val materialId: Long
)

@Serializable
data object AjustesRoute

@Serializable
data object AdminRoute

@Serializable
data class ObraAdminRoute(
    val obraId: Long,
    val nombre: String
)
