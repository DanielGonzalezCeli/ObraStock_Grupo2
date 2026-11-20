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

/**
 * Grafo de navegación de ObraStock: un único NavHost con las tres pantallas.
 * Por ahora cada destino muestra una pantalla vacía; se reemplazan en los siguientes commits.
 */
@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = BodegaRoute) {
        composable<BodegaRoute> {
            PantallaVacia(titulo = "Bodega")
        }
        composable<RegistroRoute> {
            PantallaVacia(titulo = "Registro")
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
