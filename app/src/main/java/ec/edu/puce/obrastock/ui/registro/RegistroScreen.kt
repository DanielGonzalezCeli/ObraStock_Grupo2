package ec.edu.puce.obrastock.ui.registro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Formulario para registrar un material nuevo.
 * Los campos usan rememberSaveable, así no se pierden al rotar la pantalla.
 * Por ahora solo valida; guardar en la base de datos llega con Room.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
) {
    var codigo by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var unidad by rememberSaveable { mutableStateOf("") }
    var cantidad by rememberSaveable { mutableStateOf("") }
    // Los errores solo se muestran después del primer intento de guardar
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    val errores = ValidadorRegistro.validar(FormularioRegistro(codigo, nombre, unidad, cantidad))
    val mostrar = if (intentoGuardar) errores else ErroresRegistro()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar material") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CampoTexto("Código (ej. CEM-001)", codigo, mostrar.codigo) { codigo = it.take(20) }
            CampoTexto("Nombre del material", nombre, mostrar.nombre) {
                nombre = it.take(ValidadorRegistro.MAX_NOMBRE)
            }

            Text("Unidad", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValidadorRegistro.UNIDADES.forEach { opcion ->
                    FilterChip(
                        selected = unidad == opcion,
                        onClick = { unidad = opcion },
                        label = { Text(opcion) },
                    )
                }
            }
            mostrar.unidad?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            CampoTexto(
                etiqueta = "Cantidad inicial",
                valor = cantidad,
                error = mostrar.cantidad,
                teclado = KeyboardType.Decimal,
            ) { cantidad = it.take(12) }

            Button(
                onClick = {
                    intentoGuardar = true
                    if (!errores.hayErrores) onGuardado()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Guardar")
            }
        }
    }
}

/** Campo de texto con su mensaje de error debajo. */
@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    error: String?,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth(),
    )
}
