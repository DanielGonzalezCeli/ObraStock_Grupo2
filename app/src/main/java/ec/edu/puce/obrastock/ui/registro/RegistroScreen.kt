package ec.edu.puce.obrastock.ui.registro

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.edu.puce.obrastock.domain.model.DatosFactura
import ec.edu.puce.obrastock.domain.model.Material
import ec.edu.puce.obrastock.domain.model.MaterialEnBodega
import ec.edu.puce.obrastock.domain.model.Obra
import ec.edu.puce.obrastock.domain.model.Rol
import ec.edu.puce.obrastock.domain.model.TipoMovimiento
import ec.edu.puce.obrastock.domain.model.Usuario
import ec.edu.puce.obrastock.ui.common.AppViewModelFactory
import ec.edu.puce.obrastock.ui.common.BotonOscuro
import ec.edu.puce.obrastock.ui.common.BotonPrincipal
import ec.edu.puce.obrastock.ui.common.CampoFila
import ec.edu.puce.obrastock.ui.common.EstadoVacio
import ec.edu.puce.obrastock.ui.common.SelectorPildoras
import ec.edu.puce.obrastock.ui.common.TarjetaContorno
import ec.edu.puce.obrastock.ui.common.TituloSeccion
import ec.edu.puce.obrastock.ui.common.UiState
import ec.edu.puce.obrastock.ui.common.formatearCantidad
import ec.edu.puce.obrastock.ui.common.formatearDinero
import ec.edu.puce.obrastock.ui.common.formatearFecha
import ec.edu.puce.obrastock.ui.common.marco
import ec.edu.puce.obrastock.ui.theme.ObraStockTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** Stateful: conecta el ViewModel, los permisos de cámara y la navegación al guardar. */
@Composable
fun RegistroScreen(
    onGuardado: () -> Unit,
    viewModel: RegistroViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cantidad by viewModel.cantidad.collectAsStateWithLifecycle()
    val costo by viewModel.costoTotal.collectAsStateWithLifecycle()
    val proveedor by viewModel.proveedor.collectAsStateWithLifecycle()
    val modoCamara by viewModel.modoCamara.collectAsStateWithLifecycle()
    val deteccion by viewModel.deteccionFactura.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Permiso de cámara en tiempo de ejecución; el modo pedido se recuerda al rotar
    var modoPendiente by rememberSaveable { mutableStateOf(ModoCamara.APAGADA) }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) viewModel.abrirCamara(modoPendiente)
    }
    val abrirCamara: (ModoCamara) -> Unit = { modo ->
        val tiene = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (tiene) viewModel.abrirCamara(modo) else {
            modoPendiente = modo
            pedirPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(state.guardado) {
        when (val g = state.guardado) {
            is UiState.Success -> {
                viewModel.onGuardadoConsumido()
                onGuardado()
            }
            is UiState.Error -> snackbar.showSnackbar(g.mensaje)
            else -> Unit
        }
    }
    LaunchedEffect(aviso) {
        aviso?.let {
            viewModel.onAvisoMostrado()
            snackbar.showSnackbar(it)
        }
    }

    RegistroContent(
        state = state,
        cantidad = cantidad,
        costoTotal = costo,
        proveedor = proveedor,
        modoCamara = modoCamara,
        deteccion = deteccion,
        snackbarHostState = snackbar,
        onAbrirCamara = abrirCamara,
        onCerrarCamara = viewModel::cerrarCamara,
        onLineas = viewModel::onLineasReconocidas,
        onCodigo = viewModel::onCodigoDetectado,
        onUsarFactura = viewModel::usarDatosFactura,
        onMaterialChange = viewModel::onMaterialChange,
        onTipoChange = viewModel::onTipoChange,
        onCantidadChange = viewModel::onCantidadChange,
        onCostoChange = viewModel::onCostoChange,
        onProveedorChange = viewModel::onProveedorChange,
        onFechaChange = viewModel::onFechaChange,
        onGuardar = viewModel::guardar
    )
}

/** Stateless: solo dibuja y eleva eventos (state hoisting). */
@Composable
fun RegistroContent(
    state: RegistroUiState,
    cantidad: String,
    costoTotal: String,
    proveedor: String,
    modoCamara: ModoCamara,
    deteccion: DatosFactura?,
    snackbarHostState: SnackbarHostState,
    onAbrirCamara: (ModoCamara) -> Unit,
    onCerrarCamara: () -> Unit,
    onLineas: (List<String>) -> Unit,
    onCodigo: (String) -> Unit,
    onUsarFactura: () -> Unit,
    onMaterialChange: (Long) -> Unit,
    onTipoChange: (TipoMovimiento) -> Unit,
    onCantidadChange: (String) -> Unit,
    onCostoChange: (String) -> Unit,
    onProveedorChange: (String) -> Unit,
    onFechaChange: (Long) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarCamaraReal: Boolean = true
) {
    val esCompra = state.formulario.tipo == TipoMovimiento.COMPRA

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (state.obra == null || state.materiales.isEmpty()) {
            EstadoVacio(
                icono = Icons.AutoMirrored.Outlined.ReceiptLong,
                titulo = "Nada que registrar todavía",
                mensaje = "Primero debe existir una obra con materiales en su bodega.",
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            PanelCamara(
                modo = modoCamara,
                deteccion = deteccion,
                puedeLeerFactura = TipoMovimiento.COMPRA in state.tiposPermitidos,
                mostrarCamaraReal = mostrarCamaraReal,
                onAbrir = onAbrirCamara,
                onCerrar = onCerrarCamara,
                onLineas = onLineas,
                onCodigo = onCodigo,
                onUsarFactura = onUsarFactura
            )

            // Tarjeta de datos que sube sobre el panel oscuro, como en el diseño
            TarjetaContorno(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp)
                    .fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (esCompra) {
                        TituloSeccion(if (state.formulario.textoFactura != null) "Datos extraídos" else "Datos de la factura")
                        CampoFila(
                            etiqueta = "Proveedor",
                            valor = proveedor,
                            onValorChange = onProveedorChange,
                            marcador = "Nombre del proveedor",
                            error = state.errores.proveedor,
                            teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                        )
                        CampoFecha(state.formulario.fecha, onFechaChange)
                        CampoFila(
                            etiqueta = "Total",
                            valor = costoTotal,
                            onValorChange = onCostoChange,
                            prefijo = "$ ",
                            marcador = "0,00",
                            error = state.errores.costo,
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                        )
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(6.dp))
                    }
                    TituloSeccion("Material")
                    SelectorMaterial(state.materiales, state.materialSeleccionado, state.errores.material, onMaterialChange)
                    CampoFila(
                        etiqueta = "Cantidad",
                        valor = cantidad,
                        onValorChange = onCantidadChange,
                        marcador = "0",
                        sufijo = state.materialSeleccionado?.material?.unidad,
                        error = state.errores.cantidad,
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                    )
                    state.materialSeleccionado?.let {
                        Text(
                            "En bodega: ${formatearCantidad(it.existencias)} ${it.material.unidad}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!esCompra) CampoFecha(state.formulario.fecha, onFechaChange)

                    Spacer(Modifier.height(12.dp))
                    SelectorPildoras(
                        opciones = listOf("Compra", "Consumo"),
                        seleccionada = if (esCompra) 0 else 1,
                        onSeleccion = { onTipoChange(if (it == 0) TipoMovimiento.COMPRA else TipoMovimiento.CONSUMO) },
                        habilitadas = listOf(
                            TipoMovimiento.COMPRA in state.tiposPermitidos,
                            TipoMovimiento.CONSUMO in state.tiposPermitidos
                        )
                    )
                    if (TipoMovimiento.COMPRA !in state.tiposPermitidos) {
                        Text(
                            "Como ${state.usuario?.rol?.etiqueta?.lowercase()} solo registras consumos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    state.errores.tipo?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(8.dp))
                    BotonOscuro(
                        texto = "Confirmar y guardar",
                        onClick = onGuardar,
                        cargando = state.guardado is UiState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Panel oscuro superior: invita a escanear o muestra la cámara con el marco naranja. */
@Composable
private fun PanelCamara(
    modo: ModoCamara,
    deteccion: DatosFactura?,
    puedeLeerFactura: Boolean,
    mostrarCamaraReal: Boolean,
    onAbrir: (ModoCamara) -> Unit,
    onCerrar: () -> Unit,
    onLineas: (List<String>) -> Unit,
    onCodigo: (String) -> Unit,
    onUsarFactura: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.inverseSurface)
            .statusBarsPadding()
            .height(if (modo == ModoCamara.APAGADA) 250.dp else 360.dp)
    ) {
        if (modo == ModoCamara.APAGADA) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.DocumentScanner, contentDescription = null, tint = c.inverseOnSurface, modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    "Escanea la factura o el código del material",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.inverseOnSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    "La IA lee los datos en el teléfono, sin internet",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.inverseOnSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (puedeLeerFactura) {
                        BotonPrincipal("Leer factura", { onAbrir(ModoCamara.FACTURA) }, icono = Icons.AutoMirrored.Outlined.ReceiptLong)
                    }
                    OutlinedButton(
                        onClick = { onAbrir(ModoCamara.CODIGO) },
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, c.inverseOnSurface.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = c.inverseOnSurface),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Leer código", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        } else {
            if (mostrarCamaraReal) {
                VistaCamara(modo = modo, onLineas = onLineas, onCodigo = onCodigo, modifier = Modifier.fillMaxSize())
            }
            val detectada = modo == ModoCamara.FACTURA && deteccion?.tieneDatos == true
            // Marco naranja del diseño
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp, end = 40.dp, top = 20.dp, bottom = 48.dp)
                    .marco(if (detectada) c.primary else c.inverseOnSurface.copy(alpha = 0.6f))
            )
            Surface(
                onClick = onCerrar,
                shape = CircleShape,
                color = c.inverseSurface.copy(alpha = 0.6f),
                contentColor = c.inverseOnSurface,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Close, contentDescription = "Cerrar cámara") }
            }
            // Píldora inferior: "Factura detectada" se toca para usar los datos
            Surface(
                onClick = { if (detectada) onUsarFactura() },
                enabled = detectada,
                shape = RoundedCornerShape(50),
                color = if (detectada) c.primary else c.inverseSurface.copy(alpha = 0.75f),
                contentColor = if (detectada) c.onPrimary else c.inverseOnSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp)
            ) {
                Text(
                    text = when {
                        modo == ModoCamara.CODIGO -> "Apunta al código de barras"
                        detectada -> "Factura detectada · " + listOfNotNull(
                            deteccion?.totalCentavos?.let { formatearDinero(it) }
                        ).joinToString() + "  Usar datos"
                        else -> "Apunta a la factura"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectorMaterial(
    materiales: List<MaterialEnBodega>,
    seleccionado: MaterialEnBodega?,
    error: String?,
    onMaterialChange: (Long) -> Unit
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    Box {
        CampoFila(
            etiqueta = "Material",
            valor = seleccionado?.material?.nombre ?: "",
            onValorChange = {},
            marcador = "Elegir ▾",
            error = error,
            onClick = { abierto = true }
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            materiales.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.material.nombre, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "${formatearCantidad(item.existencias)} ${item.material.unidad}",
                                color = if (item.stockBajo) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onMaterialChange(item.material.id)
                        abierto = false
                    }
                )
            }
        }
    }
}

/** Fecha con selector de calendario (Material 3). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoFecha(fecha: Long, onFechaChange: (Long) -> Unit) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    CampoFila(
        etiqueta = "Fecha",
        valor = formatearFecha(fecha),
        onValorChange = {},
        onClick = { abierto = true }
    )
    if (abierto) {
        val zona = ZoneId.systemDefault()
        // El DatePicker trabaja en UTC: se convierte el día local a medianoche UTC y viceversa
        val inicial = Instant.ofEpochMilli(fecha).atZone(zona).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = inicial,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { utc ->
                        val dia = Instant.ofEpochMilli(utc).atZone(ZoneOffset.UTC).toLocalDate()
                        onFechaChange(dia.atTime(12, 0).atZone(zona).toInstant().toEpochMilli())
                    }
                    abierto = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = estado)
        }
    }
}

@Preview(showBackground = true, heightDp = 820)
@Composable
private fun RegistroPreview() {
    val cemento = MaterialEnBodega(Material(1, "Cemento", "Cemento", "sacos"), 1, 40.0, 20.0)
    ObraStockTheme(dynamicColor = false) {
        RegistroContent(
            state = RegistroUiState(
                obra = Obra(1, "Casa Quito Norte", "Quito"),
                usuario = Usuario("u", "Ana", "ana@obra.ec", Rol.BODEGUERO),
                materiales = listOf(cemento),
                formulario = FormularioRegistro(materialId = 1, fecha = LocalDate.of(2026, 10, 12).toEpochDay() * 86_400_000, textoFactura = "...")
            ),
            cantidad = "40",
            costoTotal = "486,50",
            proveedor = "Ferretería El Constructor",
            modoCamara = ModoCamara.FACTURA,
            deteccion = DatosFactura("FERRETERIA EL CONSTRUCTOR", null, 48_650),
            snackbarHostState = remember { SnackbarHostState() },
            onAbrirCamara = {}, onCerrarCamara = {}, onLineas = {}, onCodigo = {}, onUsarFactura = {},
            onMaterialChange = {}, onTipoChange = {}, onCantidadChange = {}, onCostoChange = {},
            onProveedorChange = {}, onFechaChange = {}, onGuardar = {},
            mostrarCamaraReal = false
        )
    }
}
