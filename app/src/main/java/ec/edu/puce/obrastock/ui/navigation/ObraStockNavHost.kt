package ec.edu.puce.obrastock.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ec.edu.puce.obrastock.domain.model.Permisos
import ec.edu.puce.obrastock.domain.model.Usuario
import ec.edu.puce.obrastock.ui.AppViewModel
import ec.edu.puce.obrastock.ui.EstadoSesion
import ec.edu.puce.obrastock.ui.admin.AdminScreen
import ec.edu.puce.obrastock.ui.admin.ObraAdminScreen
import ec.edu.puce.obrastock.ui.ajustes.AjustesScreen
import ec.edu.puce.obrastock.ui.auth.LoginScreen
import ec.edu.puce.obrastock.ui.bodega.BodegaScreen
import ec.edu.puce.obrastock.ui.common.CargandoPantalla
import ec.edu.puce.obrastock.ui.detalle.DetalleMaterialScreen
import ec.edu.puce.obrastock.ui.historial.HistorialScreen
import ec.edu.puce.obrastock.ui.registro.RegistroScreen
import ec.edu.puce.obrastock.ui.resumen.ResumenScreen
import kotlin.reflect.KClass

private data class DestinoPrincipal(
    val etiqueta: String,
    val icono: ImageVector,
    val ruta: Any,
    val clase: KClass<*>
)

private fun NavDestination?.esParteDe(clase: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(clase) } == true

/** Navega a una pestaña sin apilar copias y conservando el estado de cada pestaña. */
private fun NavHostController.irAPestana(ruta: Any) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Raíz: sin sesión muestra el login; con sesión, la app según el rol. */
@Composable
fun ObraStockAppRoot(appViewModel: AppViewModel) {
    val sesion by appViewModel.sesion.collectAsStateWithLifecycle()
    when (val s = sesion) {
        EstadoSesion.Cargando -> CargandoPantalla()
        EstadoSesion.SinSesion -> LoginScreen()
        // key: al cambiar de cuenta se descarta la pila de navegación anterior
        is EstadoSesion.Activa -> key(s.usuario.id) { AppPrincipal(s.usuario) }
    }
}

@Composable
private fun AppPrincipal(usuario: Usuario) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination

    // Barra inferior del diseño: Bodega · Registro · Resumen (Resumen solo si el rol lo permite)
    val destinos = buildList {
        add(DestinoPrincipal("Bodega", Icons.Outlined.Warehouse, BodegaRoute, BodegaRoute::class))
        add(DestinoPrincipal("Registro", Icons.Outlined.PhotoCamera, RegistroRoute(), RegistroRoute::class))
        if (Permisos.puedeVerResumen(usuario.rol)) {
            add(DestinoPrincipal("Resumen", Icons.Outlined.BarChart, ResumenRoute, ResumenRoute::class))
        }
    }
    val mostrarBarra = destinos.any { destinoActual.esParteDe(it.clase) }

    // El Scaffold raíz no aplica insets al contenido: cada pantalla gestiona los suyos.
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (mostrarBarra) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                        destinos.forEach { destino ->
                            NavigationBarItem(
                                selected = destinoActual.esParteDe(destino.clase),
                                onClick = { navController.irAPestana(destino.ruta) },
                                icon = { Icon(destino.icono, contentDescription = null) },
                                label = { Text(destino.etiqueta, style = MaterialTheme.typography.labelLarge) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BodegaRoute,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            composable<BodegaRoute> {
                BodegaScreen(
                    onRegistrar = { navController.irAPestana(RegistroRoute()) },
                    onVerMaterial = { obraId, materialId -> navController.navigate(DetalleMaterialRoute(obraId, materialId)) },
                    onRegistrarMaterial = { materialId, tipo ->
                        navController.navigate(RegistroRoute(materialId = materialId, tipo = tipo))
                    },
                    onAjustes = { navController.navigate(AjustesRoute) },
                    onAdministrar = { navController.navigate(AdminRoute) }
                )
            }
            composable<RegistroRoute> {
                RegistroScreen(onGuardado = {
                    // "Guardar y ver": como en el diseño, se pasa al resumen (o a la bodega si el rol no lo ve)
                    navController.irAPestana(if (Permisos.puedeVerResumen(usuario.rol)) ResumenRoute else BodegaRoute)
                })
            }
            composable<ResumenRoute> {
                ResumenScreen(onHistorial = { navController.navigate(HistorialRoute) })
            }
            composable<HistorialRoute> {
                HistorialScreen(onVolver = { navController.popBackStack() })
            }
            composable<DetalleMaterialRoute> {
                DetalleMaterialScreen(
                    onVolver = { navController.popBackStack() },
                    onRegistrarMovimiento = { materialId, tipo ->
                        navController.navigate(RegistroRoute(materialId = materialId, tipo = tipo))
                    }
                )
            }
            composable<AjustesRoute> {
                AjustesScreen(
                    onVolver = { navController.popBackStack() },
                    onAdministrar = { navController.navigate(AdminRoute) }
                )
            }
            composable<AdminRoute> {
                AdminScreen(
                    onVolver = { navController.popBackStack() },
                    onConfigurarObra = { obra -> navController.navigate(ObraAdminRoute(obra.id, obra.nombre)) }
                )
            }
            composable<ObraAdminRoute> {
                ObraAdminScreen(onVolver = { navController.popBackStack() })
            }
        }
    }
}
