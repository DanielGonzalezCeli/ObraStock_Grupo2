package ec.edu.puce.obrastock.ui.registro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ec.edu.puce.obrastock.ui.ejemplo.DatosEjemplo

data class FormularioMaterial(
    val nombre: String = "",
    val categoria: String = "",
    val unidad: String = "",
    val existencias: String = "",
    val existenciasMinimas: String = "",
    val costoUnitario: String = ""
)

// Versión con estado: guarda lo escrito aunque se gire la pantalla.
// En la Fase 2 se guarda un borrador y el botón Guardar llama al repositorio.
@Composable
fun RegistroRoute(
    materialId: Long?,
    onTerminar: () -> Unit
) {
    val inicial = materialId?.let { DatosEjemplo.buscarMaterial(it) }

    var nombre by rememberSaveable(materialId) { mutableStateOf(inicial?.nombre ?: "") }
    var categoria by rememberSaveable(materialId) { mutableStateOf(inicial?.categoria ?: "") }
    var unidad by rememberSaveable(materialId) { mutableStateOf(inicial?.unidad ?: "") }
    var existencias by rememberSaveable(materialId) { mutableStateOf(inicial?.existencias?.toString() ?: "") }
    var minimas by rememberSaveable(materialId) { mutableStateOf(inicial?.existenciasMinimas?.toString() ?: "") }
    var costo by rememberSaveable(materialId) { mutableStateOf(inicial?.costoUnitario?.toString() ?: "") }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }

    val formulario = FormularioMaterial(nombre, categoria, unidad, existencias, minimas, costo)

    RegistroContent(
        titulo = if (materialId == null) "Nuevo material" else "Editar material",
        formulario = formulario,
        mostrarErrores = mostrarErrores,
        onCambio = { nuevo ->
            nombre = nuevo.nombre
            categoria = nuevo.categoria
            unidad = nuevo.unidad
            existencias = nuevo.existencias
            minimas = nuevo.existenciasMinimas
            costo = nuevo.costoUnitario
        },
        onGuardar = {
            mostrarErrores = true
            if (formularioValido(formulario)) {
                onTerminar()
            }
        },
        onVolver = onTerminar
    )
}

private fun formularioValido(f: FormularioMaterial): Boolean =
    f.nombre.isNotBlank() &&
        f.categoria.isNotBlank() &&
        f.unidad.isNotBlank() &&
        ValidacionMaterial.leerNumero(f.existencias) != null &&
        ValidacionMaterial.leerNumero(f.existenciasMinimas) != null &&
        ValidacionMaterial.leerNumero(f.costoUnitario) != null

// Versión sin estado: solo dibuja el formulario que recibe.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroContent(
    titulo: String,
    formulario: FormularioMaterial,
    mostrarErrores: Boolean,
    onCambio: (FormularioMaterial) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titulo) },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoTexto(
                valor = formulario.nombre,
                etiqueta = "Nombre",
                error = mostrarErrores && formulario.nombre.isBlank(),
                mensajeError = "Escribe el nombre del material",
                onCambio = { onCambio(formulario.copy(nombre = it)) }
            )
            CampoTexto(
                valor = formulario.categoria,
                etiqueta = "Categoría",
                error = mostrarErrores && formulario.categoria.isBlank(),
                mensajeError = "Escribe la categoría",
                onCambio = { onCambio(formulario.copy(categoria = it)) }
            )
            CampoTexto(
                valor = formulario.unidad,
                etiqueta = "Unidad (saco, m³, unidad...)",
                error = mostrarErrores && formulario.unidad.isBlank(),
                mensajeError = "Escribe la unidad",
                onCambio = { onCambio(formulario.copy(unidad = it)) }
            )
            CampoTexto(
                valor = formulario.existencias,
                etiqueta = "Existencias",
                error = mostrarErrores && ValidacionMaterial.leerNumero(formulario.existencias) == null,
                mensajeError = "Escribe un número igual o mayor que cero",
                numerico = true,
                onCambio = { onCambio(formulario.copy(existencias = it)) }
            )
            CampoTexto(
                valor = formulario.existenciasMinimas,
                etiqueta = "Existencias mínimas",
                error = mostrarErrores && ValidacionMaterial.leerNumero(formulario.existenciasMinimas) == null,
                mensajeError = "Escribe un número igual o mayor que cero",
                numerico = true,
                onCambio = { onCambio(formulario.copy(existenciasMinimas = it)) }
            )
            CampoTexto(
                valor = formulario.costoUnitario,
                etiqueta = "Costo unitario",
                error = mostrarErrores && ValidacionMaterial.leerNumero(formulario.costoUnitario) == null,
                mensajeError = "Escribe un número igual o mayor que cero",
                numerico = true,
                onCambio = { onCambio(formulario.copy(costoUnitario = it)) }
            )
            Button(onClick = onGuardar, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    etiqueta: String,
    error: Boolean,
    mensajeError: String,
    onCambio: (String) -> Unit,
    numerico: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error,
        supportingText = { if (error) Text(mensajeError) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numerico) KeyboardType.Decimal else KeyboardType.Text
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
