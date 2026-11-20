package ec.edu.puce.obrastock.ui.navigation

import kotlinx.serialization.Serializable

/*
 * Rutas tipadas de la app. Cada pantalla es un tipo @Serializable,
 * así el compilador revisa los destinos y los argumentos (no hay strings sueltos).
 */

/** Pantalla principal: lista de materiales de la bodega. */
@Serializable
object BodegaRoute

/** Formulario para registrar un material nuevo. */
@Serializable
object RegistroRoute

/** Detalle de un material; recibe su id como argumento tipado. */
@Serializable
data class DetalleRoute(val materialId: Int)
