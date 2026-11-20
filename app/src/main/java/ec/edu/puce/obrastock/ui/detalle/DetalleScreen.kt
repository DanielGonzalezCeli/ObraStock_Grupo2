package ec.edu.puce.obrastock.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ec.edu.puce.obrastock.domain.model.Material
import ec.edu.puce.obrastock.domain.model.Movimiento
import ec.edu.puce.obrastock.ui.ejemplo.DatosEjemplo

// Versión con estado. En la Fase 2 el estado de sincronización lo maneja un ViewModel;
// por ahora los botones de abajo solo permiten ver cómo se dibuja cada estado.
@Composable
fun DetalleRoute(
    materialId: Long,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val material = remember(materialId) { DatosEjemplo.buscarMaterial(materialId) }
    val movimientos = remember(materialId) { DatosEjemplo.movimientosDe(materialId) }
    var estado by remember { mutableStateOf<SyncUiState>(SyncUiState.Idle) }

    DetalleContent(
        material = material,
        movimientos = movimientos,
        estado = estado,
        onCambiarEstado = { estado = it },
        onEditar = { onEditar(materialId) },
        onVolver = onVolver
    )
}

// Versión sin estado.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContent(
    material: Material?,
    movimientos: List<Movimiento>,
    estado: SyncUiState,
    onCambiarEstado: (SyncUiState) -> Unit,
    onEditar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(material?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { margenes ->
        Column(
            modifier = Modifier
                .padding(margenes)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (material == null) {
                Text("No se encontró el material.")
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Categoría: ${material.categoria}")
                        Text("Existencias: ${material.existencias} ${material.unidad}")
                        Text("Existencias mínimas: ${material.existenciasMinimas}")
                        Text("Costo unitario: ${material.costoUnitario}")
                        material.descripcion?.let { Text(it) }
                    }
                }
                Button(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                    Text("Editar material")
                }

                Text("Movimientos", style = MaterialTheme.typography.titleMedium)
                if (movimientos.isEmpty()) {
                    Text("Todavía no hay movimientos.")
                } else {
                    movimientos.forEach { movimiento ->
                        Text("${movimiento.tipo}: ${movimiento.cantidad} ${material.unidad}")
                    }
                }

                Text("Sincronización", style = MaterialTheme.typography.titleMedium)
                EstadoSincronizacion(estado)

                Text("Vista previa de estados (solo Fase 1)", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onCambiarEstado(SyncUiState.Idle) }) { Text("Reposo") }
                    OutlinedButton(onClick = { onCambiarEstado(SyncUiState.Loading) }) { Text("Cargando") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onCambiarEstado(SyncUiState.Success("Datos sincronizados")) }) {
                        Text("Éxito")
                    }
                    OutlinedButton(onClick = { onCambiarEstado(SyncUiState.Error("No hay conexión")) }) {
                        Text("Error")
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoSincronizacion(estado: SyncUiState) {
    when (estado) {
        SyncUiState.Idle -> Text("Sin sincronizar todavía.")
        SyncUiState.Loading -> CircularProgressIndicator()
        is SyncUiState.Success -> Text(estado.mensaje, color = MaterialTheme.colorScheme.primary)
        is SyncUiState.Error -> Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
    }
}
