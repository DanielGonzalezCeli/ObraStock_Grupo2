package ec.edu.puce.obrastock.ui.bodega

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ec.edu.puce.obrastock.domain.model.Material
import ec.edu.puce.obrastock.domain.model.existenciasBajas
import ec.edu.puce.obrastock.ui.ejemplo.DatosEjemplo

// Versión con estado: obtiene los datos y se los pasa a la pantalla.
// En la Fase 2 los datos vendrán de un ViewModel conectado a Room.
@Composable
fun BodegaRoute(
    onNuevoMaterial: () -> Unit,
    onAbrirMaterial: (Long) -> Unit
) {
    val materiales = remember { DatosEjemplo.materiales }
    BodegaContent(
        materiales = materiales,
        onNuevoMaterial = onNuevoMaterial,
        onAbrirMaterial = onAbrirMaterial
    )
}

// Versión sin estado: solo dibuja lo que recibe y avisa con eventos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodegaContent(
    materiales: List<Material>,
    onNuevoMaterial: () -> Unit,
    onAbrirMaterial: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Bodega") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoMaterial) {
                Icon(Icons.Default.Add, contentDescription = "Agregar material")
            }
        }
    ) { margenes ->
        LazyColumn(
            modifier = Modifier.padding(margenes),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(materiales, key = { it.id }) { material ->
                TarjetaMaterial(material = material, onClick = { onAbrirMaterial(material.id) })
            }
        }
    }
}

@Composable
private fun TarjetaMaterial(material: Material, onClick: () -> Unit) {
    val bajas = material.existenciasBajas
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (bajas) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = material.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = material.categoria, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Existencias: ${material.existencias} ${material.unidad} (mínimo ${material.existenciasMinimas})",
                style = MaterialTheme.typography.bodyMedium
            )
            if (bajas) {
                Text(
                    text = "Existencias bajas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
