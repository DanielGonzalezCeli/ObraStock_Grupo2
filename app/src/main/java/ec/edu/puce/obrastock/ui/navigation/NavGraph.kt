package ec.edu.puce.obrastock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ec.edu.puce.obrastock.ui.bodega.BodegaRoute
import ec.edu.puce.obrastock.ui.detalle.DetalleRoute
import ec.edu.puce.obrastock.ui.registro.RegistroRoute

@Composable
fun ObraStockNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Bodega) {
        composable<Bodega> {
            BodegaRoute(
                onNuevoMaterial = { navController.navigate(RegistroMaterial()) },
                onAbrirMaterial = { id -> navController.navigate(DetalleMaterial(id)) }
            )
        }
        composable<RegistroMaterial> { entrada ->
            val ruta = entrada.toRoute<RegistroMaterial>()
            RegistroRoute(
                materialId = ruta.materialId,
                onTerminar = { navController.popBackStack() }
            )
        }
        composable<DetalleMaterial> { entrada ->
            val ruta = entrada.toRoute<DetalleMaterial>()
            DetalleRoute(
                materialId = ruta.materialId,
                onEditar = { id -> navController.navigate(RegistroMaterial(id)) },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
