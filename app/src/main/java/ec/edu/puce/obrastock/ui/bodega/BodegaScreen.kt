package ec.edu.puce.obrastock.ui.bodega

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ec.edu.puce.obrastock.domain.model.DatosEjemplo
import ec.edu.puce.obrastock.domain.model.Material

/**
 * Pantalla Bodega: lista los materiales de la obra con LazyColumn.
 * Es stateless: recibe la lista y avisa los clics con lambdas (state hoisting).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodegaScreen(
    onAgregarMaterial: () -> Unit,
    onVerDetalle: (materialId: Int) -> Unit,
    materiales: List<Material> = DatosEjemplo.materiales,
    nombreObra: String = DatosEjemplo.obra.nombre,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bodega")
                        Text(nombreObra, style = MaterialTheme.typography.bodySmall)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAgregarMaterial) {
                Text("Agregar material")
            }
        },
    ) { relleno ->
        if (materiales.isEmpty()) {
            Text(
                text = "Todavía no hay materiales en la bodega",
                modifier = Modifier.padding(relleno).padding(24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // key estable: Compose no recompone toda la lista cuando cambia un elemento
                items(items = materiales, key = { it.id }) { material ->
                    TarjetaMaterial(material = material, onClick = { onVerDetalle(material.id) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaMaterial(material: Material, onClick: () -> Unit) {
    val colores = if (material.stockBajo) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(onClick = onClick, colors = colores, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(material.nombre, style = MaterialTheme.typography.titleMedium)
                Text(material.codigo, style = MaterialTheme.typography.bodySmall)
                if (material.stockBajo) {
                    Text(
                        text = "Stock bajo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Text(
                text = "${formatearCantidad(material.stock)} ${material.unidad}",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

/** Muestra 120 en lugar de 120.0, pero conserva decimales como 8.5. */
private fun formatearCantidad(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()
