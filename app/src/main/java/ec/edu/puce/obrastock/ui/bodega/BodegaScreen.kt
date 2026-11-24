package ec.edu.puce.obrastock.ui.bodega

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import ec.edu.puce.obrastock.ui.common.marco
import ec.edu.puce.obrastock.ui.detalle.DetalleMaterialScreen
import ec.edu.puce.obrastock.ui.detalle.DetalleMaterialViewModel
import ec.edu.puce.obrastock.ui.registro.ModoCamara
import ec.edu.puce.obrastock.ui.registro.VistaCamara
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.edu.puce.obrastock.domain.model.Material
import ec.edu.puce.obrastock.domain.model.MaterialEnBodega
import ec.edu.puce.obrastock.domain.model.Obra
import ec.edu.puce.obrastock.domain.model.Rol
import ec.edu.puce.obrastock.ui.common.AppViewModelFactory
import ec.edu.puce.obrastock.ui.common.AvatarInicial
import ec.edu.puce.obrastock.ui.common.BotonCircular
import ec.edu.puce.obrastock.ui.common.CargandoPantalla
import ec.edu.puce.obrastock.ui.common.ChipPildora
import ec.edu.puce.obrastock.ui.common.Encabezado
import ec.edu.puce.obrastock.ui.common.EstadoVacio
import ec.edu.puce.obrastock.ui.common.TarjetaContorno
import ec.edu.puce.obrastock.ui.common.UiState
import ec.edu.puce.obrastock.ui.common.cifras
import ec.edu.puce.obrastock.ui.common.formatearCantidad
import ec.edu.puce.obrastock.ui.theme.ObraStockTheme

/**
 * Contenedor con estado (Stateful): conecta el ViewModel con la UI pura.
 * Ventana adaptativa: en pantallas anchas (≥ 600 dp, tablets o teléfono horizontal) muestra
 * la lista y el detalle en dos columnas; en teléfonos, el detalle es otra pantalla.
 */
@Composable
fun BodegaScreen(
    onRegistrar: () -> Unit,
    onVerMaterial: (obraId: Long, materialId: Long) -> Unit,
    onRegistrarMaterial: (materialId: Long, tipo: String) -> Unit,
    onAjustes: () -> Unit,
    onAdministrar: () -> Unit,
    viewModel: BodegaViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val consulta by viewModel.consulta.collectAsStateWithLifecycle()
    val rol by viewModel.rol.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()
    val sincronizacion by viewModel.sincronizacionEstado.collectAsStateWithLifecycle()
    val sinConexion by viewModel.sinConexion.collectAsStateWithLifecycle()
    val escaneado by viewModel.materialEscaneado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    val dosColumnas = LocalConfiguration.current.screenWidthDp >= 600
    var obraSeleccionada by rememberSaveable { mutableStateOf<Long?>(null) }
    var materialSeleccionado by rememberSaveable { mutableStateOf<Long?>(null) }
    val abrirMaterial: (Long, Long) -> Unit = { obraId, materialId ->
        if (dosColumnas) {
            obraSeleccionada = obraId
            materialSeleccionado = materialId
        } else {
            onVerMaterial(obraId, materialId)
        }
    }

    // Escáner de códigos de barras (ML Kit) con permiso de cámara en tiempo de ejecución
    var escaneando by rememberSaveable { mutableStateOf(false) }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) escaneando = true
    }
    val abrirEscaner: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            escaneando = true
        } else {
            pedirPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(sincronizacion) {
        val mensaje = when (val s = sincronizacion) {
            is UiState.Success -> s.datos
            is UiState.Error -> s.mensaje
            else -> null
        }
        if (mensaje != null) {
            viewModel.onMensajeMostrado()
            snackbar.showSnackbar(mensaje)
        }
    }
    LaunchedEffect(escaneado) {
        escaneado?.let {
            viewModel.onMaterialEscaneadoAbierto()
            abrirMaterial(it.obraId, it.material.id)
        }
    }

    Row(Modifier.fillMaxSize()) {
        BodegaContent(
            state = state,
            consulta = consulta,
            esAdministrador = rol == Rol.ADMINISTRADOR,
            pendientes = pendientes,
            sinConexion = sinConexion,
            sincronizando = sincronizacion is UiState.Loading,
            snackbarHostState = snackbar,
            onConsultaChange = viewModel::onConsultaChange,
            onCategoriaChange = viewModel::onCategoriaChange,
            onObraChange = viewModel::onObraChange,
            onSincronizar = { viewModel.sincronizar() },
            onEscanear = abrirEscaner,
            onMaterialClick = { abrirMaterial(it.obraId, it.material.id) },
            onRegistrar = onRegistrar,
            onAjustes = onAjustes,
            onAdministrar = onAdministrar,
            modifier = Modifier.weight(1f)
        )
        if (dosColumnas) {
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Box(
                Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            ) {
                val o = obraSeleccionada
                val m = materialSeleccionado
                if (o != null && m != null && o == state.obraActiva?.id) {
                    PanelDetalle(o, m, onCerrar = { materialSeleccionado = null }, onRegistrarMaterial = onRegistrarMaterial)
                } else {
                    EstadoVacio(
                        icono = Icons.Outlined.Inventory2,
                        titulo = "Elige un material",
                        mensaje = "Su detalle, precio de referencia e historial aparecen aquí."
                    )
                }
            }
        }
    }

    if (escaneando) {
        DialogoEscaner(
            onCodigo = { codigo ->
                escaneando = false
                viewModel.onCodigoEscaneado(codigo)
            },
            onCerrar = { escaneando = false }
        )
    }
}

/** Panel derecho de la vista de dos columnas: reutiliza la pantalla de detalle. */
@Composable
private fun PanelDetalle(
    obraId: Long,
    materialId: Long,
    onCerrar: () -> Unit,
    onRegistrarMaterial: (Long, String) -> Unit
) {
    val propietario = checkNotNull(LocalViewModelStoreOwner.current)
    val base = (propietario as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras
        ?: CreationExtras.Empty
    val extras = MutableCreationExtras(base).apply {
        set(
            DEFAULT_ARGS_KEY,
            bundleOf(
                DetalleMaterialViewModel.ARG_OBRA to obraId,
                DetalleMaterialViewModel.ARG_MATERIAL to materialId
            )
        )
    }
    val detalleViewModel: DetalleMaterialViewModel = viewModel(
        key = "detalle_${obraId}_$materialId",
        factory = AppViewModelFactory.Factory,
        extras = extras
    )
    DetalleMaterialScreen(
        onVolver = onCerrar,
        onRegistrarMovimiento = onRegistrarMaterial,
        viewModel = detalleViewModel
    )
}

/** Cámara en un diálogo: al leer un código válido abre la ficha del material. */
@Composable
private fun DialogoEscaner(onCodigo: (String) -> Unit, onCerrar: () -> Unit) {
    var leido by remember { mutableStateOf(false) } // evita abrir dos veces con fotogramas seguidos
    val c = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onCerrar) {
        Surface(shape = MaterialTheme.shapes.large, color = c.inverseSurface) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                VistaCamara(
                    modo = ModoCamara.CODIGO,
                    onLineas = {},
                    onCodigo = { codigo ->
                        if (!leido) {
                            leido = true
                            onCodigo(codigo)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 36.dp, end = 36.dp, top = 90.dp, bottom = 110.dp)
                        .marco(c.primary)
                )
                Surface(
                    shape = CircleShape,
                    color = c.inverseSurface.copy(alpha = 0.75f),
                    contentColor = c.inverseOnSurface,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        "Apunta al código de barras del material",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
                IconButton(onClick = onCerrar, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = c.inverseOnSurface)
                }
            }
        }
    }
}

/** Representación visual pura (Stateless): recibe estado y emite eventos hacia arriba. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodegaContent(
    state: BodegaUiState,
    consulta: String,
    esAdministrador: Boolean,
    pendientes: Int,
    sinConexion: String?,
    sincronizando: Boolean,
    snackbarHostState: SnackbarHostState,
    onConsultaChange: (String) -> Unit,
    onCategoriaChange: (String?) -> Unit,
    onObraChange: (Long) -> Unit,
    onSincronizar: () -> Unit,
    onEscanear: () -> Unit,
    onMaterialClick: (MaterialEnBodega) -> Unit,
    onRegistrar: () -> Unit,
    onAjustes: () -> Unit,
    onAdministrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuObras by rememberSaveable { mutableStateOf(false) }
    val hayObra = state.obraActiva != null

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box {
                Encabezado(
                    titulo = state.obraActiva?.nombre ?: "ObraStock",
                    subtitulo = "Bodega de materiales",
                    conSelector = state.obras.size > 1,
                    onTituloClick = if (state.obras.size > 1) ({ menuObras = true }) else null,
                    acciones = { BotonCircular(Icons.Outlined.Settings, "Ajustes", onAjustes) }
                )
                DropdownMenu(expanded = menuObras, onDismissRequest = { menuObras = false }) {
                    state.obras.forEach { obra ->
                        DropdownMenuItem(
                            text = { Text(obra.nombre, fontWeight = if (obra.id == state.obraActiva?.id) FontWeight.Bold else null) },
                            onClick = {
                                menuObras = false
                                onObraChange(obra.id)
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (hayObra && state.totalEnBodega > 0) {
                FloatingActionButton(
                    onClick = onRegistrar,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Registrar movimiento", modifier = Modifier.size(34.dp))
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = sincronizando,
            onRefresh = onSincronizar,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.materiales is UiState.Loading -> CargandoPantalla()
                !hayObra -> EstadoVacio(
                    icono = Icons.Outlined.Warehouse,
                    titulo = "Aún no tienes obras",
                    mensaje = if (esAdministrador) "Crea la primera obra y asigna a tu equipo."
                    else "Pide al administrador que te asigne a una obra. Desliza hacia abajo para actualizar.",
                    accion = if (esAdministrador) "Crear obra" else null,
                    onAccion = onAdministrar
                )
                state.totalEnBodega == 0 -> EstadoVacio(
                    icono = Icons.Outlined.Inventory2,
                    titulo = "La bodega está vacía",
                    mensaje = if (esAdministrador) "Agrega los materiales que maneja esta obra y su mínimo de alerta."
                    else "El administrador aún no configura los materiales de esta obra.",
                    accion = if (esAdministrador) "Configurar bodega" else null,
                    onAccion = onAdministrar
                )
                else -> ContenidoBodega(
                    state, consulta, pendientes, sinConexion,
                    onConsultaChange, onCategoriaChange, onSincronizar, onEscanear, onMaterialClick
                )
            }
        }
    }
}

@Composable
private fun ContenidoBodega(
    state: BodegaUiState,
    consulta: String,
    pendientes: Int,
    sinConexion: String?,
    onConsultaChange: (String) -> Unit,
    onCategoriaChange: (String?) -> Unit,
    onSincronizar: () -> Unit,
    onEscanear: () -> Unit,
    onMaterialClick: (MaterialEnBodega) -> Unit
) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = consulta,
            onValueChange = onConsultaChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = { Text("Buscar material") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (consulta.isNotEmpty()) {
                    IconButton(onClick = { onConsultaChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                    }
                } else {
                    // Lector de código de barras: abre la ficha del material
                    IconButton(onClick = onEscanear) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = "Escanear código de barras")
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = c.surfaceContainerLowest,
                focusedContainerColor = c.surfaceContainerLowest,
                unfocusedBorderColor = c.outlineVariant
            )
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "todo") {
                ChipPildora("Todo", state.categoriaSeleccionada == null, { onCategoriaChange(null) })
            }
            items(state.categorias, key = { it }) { categoria ->
                ChipPildora(categoria, categoria == state.categoriaSeleccionada, { onCategoriaChange(categoria) })
            }
        }

        if (sinConexion != null) {
            Surface(
                color = c.surfaceContainerHigh,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Row(Modifier.padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CloudOff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Sin conexión · últimos datos guardados",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSincronizar) { Text("Reintentar") }
                }
            }
        }

        if (pendientes > 0) {
            Surface(
                onClick = onSincronizar,
                color = c.secondaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (pendientes == 1) "1 movimiento por sincronizar · toca para enviar"
                        else "$pendientes movimientos por sincronizar · toca para enviar",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        when (val materiales = state.materiales) {
            is UiState.Error -> EstadoVacio(Icons.Outlined.SearchOff, "No se pudo cargar", materiales.mensaje)
            is UiState.Success -> if (materiales.datos.isEmpty()) {
                EstadoVacio(Icons.Outlined.SearchOff, "Sin resultados", "No hay materiales que coincidan con «$consulta».")
            } else {
                LazyColumn(
                    // Espacio inferior para que el botón + no tape la última tarjeta
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = materiales.datos,
                        key = { it.material.id },       // llave estable: Compose omite filas sin cambios
                        contentType = { "material" }
                    ) { item ->
                        TarjetaMaterial(item = item, onClick = { onMaterialClick(item) })
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
fun TarjetaMaterial(
    item: MaterialEnBodega,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    TarjetaContorno(modifier = modifier.fillMaxWidth(), alerta = item.stockBajo, onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInicial(item.material.nombre, alerta = item.stockBajo)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.material.nombre, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.size(4.dp))
                if (item.stockBajo) {
                    Text(
                        "Stock bajo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.onTertiaryContainer
                    )
                } else {
                    Text(
                        "Mínimo ${formatearCantidad(item.minimo)} ${item.material.unidad}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${formatearCantidad(item.existencias)} ${item.material.unidad}",
                style = MaterialTheme.typography.titleMedium.cifras,
                color = if (item.stockBajo) c.onTertiaryContainer else c.onSurface
            )
        }
    }
}

// ---------- Vista previa con datos de ejemplo (solo para el editor, no se cargan en la app) ----------

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun BodegaPreview() {
    ObraStockTheme(dynamicColor = false) {
        BodegaContent(
            state = BodegaUiState(
                obras = listOf(Obra(1, "Casa Quito Norte", "Quito"), Obra(2, "Edificio Carolina", "Quito")),
                obraActiva = Obra(1, "Casa Quito Norte", "Quito"),
                categorias = listOf("Cemento", "Hierro", "Acabados"),
                materiales = UiState.Success(
                    listOf(
                        MaterialEnBodega(Material(1, "Cemento", "Cemento", "sacos"), 1, 40.0, 20.0),
                        MaterialEnBodega(Material(2, "Varilla 12 mm", "Hierro", "u"), 1, 3.0, 10.0),
                        MaterialEnBodega(Material(3, "Arena fina", "Áridos", "m³"), 1, 8.0, 4.0),
                        MaterialEnBodega(Material(4, "Ladrillo", "Mampostería", "u"), 1, 1200.0, 400.0)
                    )
                ),
                totalEnBodega = 4
            ),
            consulta = "",
            esAdministrador = true,
            pendientes = 0,
            sinConexion = null,
            sincronizando = false,
            snackbarHostState = remember { SnackbarHostState() },
            onConsultaChange = {},
            onCategoriaChange = {},
            onObraChange = {},
            onSincronizar = {},
            onEscanear = {},
            onMaterialClick = {},
            onRegistrar = {},
            onAjustes = {},
            onAdministrar = {}
        )
    }
}
