package ec.edu.puce.obrastock.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ec.edu.puce.obrastock.ui.bodega.BodegaScreen
import ec.edu.puce.obrastock.ui.registro.RegistroScreen

/**
 * Grafo de navegación de ObraStock: un único NavHost con las tres pantallas.
 * Las pantallas que aún no existen muestran un marcador temporal.
 */
@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = BodegaRoute) {
        composable<BodegaRoute> {
            BodegaScreen(
                onAgregarMaterial = { navController.navigate(RegistroRoute) },
                onVerDetalle = { id -> navController.navigate(DetalleRoute(materialId = id)) },
            )
        }
        composable<RegistroRoute> {
            RegistroScreen(
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() },
            )
        }
        composable<DetalleRoute> { entrada ->
            val ruta = entrada.toRoute<DetalleRoute>()
            PantallaVacia(titulo = "Detalle del material ${ruta.materialId}")
        }
    }
}

/** Marcador temporal mientras se construye cada pantalla. */
@Composable
private fun PantallaVacia(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = titulo, style = MaterialTheme.typography.headlineSmall)
    }
}
