package ec.edu.puce.obrastock.ui.registro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.puce.obrastock.domain.model.DatosFactura
import ec.edu.puce.obrastock.domain.model.MaterialEnBodega
import ec.edu.puce.obrastock.domain.model.Obra
import ec.edu.puce.obrastock.domain.model.Permisos
import ec.edu.puce.obrastock.domain.model.TipoMovimiento
import ec.edu.puce.obrastock.domain.model.Usuario
import ec.edu.puce.obrastock.domain.repository.AuthRepository
import ec.edu.puce.obrastock.domain.repository.InventarioRepository
import ec.edu.puce.obrastock.domain.repository.ObraRepository
import ec.edu.puce.obrastock.domain.repository.SincronizacionRepository
import ec.edu.puce.obrastock.domain.usecase.EntradaMovimiento
import ec.edu.puce.obrastock.domain.usecase.ErroresMovimiento
import ec.edu.puce.obrastock.domain.usecase.ExtraerDatosFacturaUseCase
import ec.edu.puce.obrastock.domain.usecase.RegistrarMovimientoUseCase
import ec.edu.puce.obrastock.domain.usecase.ResultadoValidacion
import ec.edu.puce.obrastock.ui.common.UiState
import ec.edu.puce.obrastock.ui.navigation.RegistroRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

enum class ModoCamara { APAGADA, FACTURA, CODIGO }

/** Valores del formulario tal como se escriben. */
data class FormularioRegistro(
    val materialId: Long? = null,
    val tipo: TipoMovimiento = TipoMovimiento.COMPRA,
    val fecha: Long = System.currentTimeMillis(),
    val textoFactura: String? = null
)

data class RegistroUiState(
    val obra: Obra? = null,
    val usuario: Usuario? = null,
    val materiales: List<MaterialEnBodega> = emptyList(),
    val formulario: FormularioRegistro = FormularioRegistro(),
    val errores: ErroresMovimiento = ErroresMovimiento(),
    val guardado: UiState<Long> = UiState.Idle
) {
    val materialSeleccionado: MaterialEnBodega?
        get() = materiales.firstOrNull { it.material.id == formulario.materialId }

    val tiposPermitidos: List<TipoMovimiento>
        get() = usuario?.let { Permisos.tiposPermitidos(it.rol) } ?: emptyList()
}

/**
 * Formulario ultra-resiliente: CADA campo se guarda en SavedStateHandle al escribirlo,
 * así sobrevive a la rotación y a la muerte del proceso. Los argumentos de RegistroRoute
 * (materialId, tipo) llegan en el mismo SavedStateHandle y sirven de valor inicial.
 * También recibe lo que la IA lee con la cámara (factura o código de barras).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModel(
    private val savedStateHandle: SavedStateHandle,
    obraRepository: ObraRepository,
    inventarioRepository: InventarioRepository,
    authRepository: AuthRepository,
    private val sincronizacion: SincronizacionRepository,
    private val registrarMovimiento: RegistrarMovimientoUseCase,
    private val extraerDatosFactura: ExtraerDatosFacturaUseCase
) : ViewModel() {

    // Campos de texto expuestos directamente (sin pasar por combine) para que el cursor no salte.
    val cantidad: StateFlow<String> = savedStateHandle.getStateFlow(KEY_CANTIDAD, "")
    val costoTotal: StateFlow<String> = savedStateHandle.getStateFlow(KEY_COSTO, "")
    val proveedor: StateFlow<String> = savedStateHandle.getStateFlow(KEY_PROVEEDOR, "")

    /** El modo de cámara también se conserva: al rotar, la cámara sigue abierta. */
    val modoCamara: StateFlow<ModoCamara> = savedStateHandle.getStateFlow(KEY_CAMARA, ModoCamara.APAGADA.name)
        .map { ModoCamara.valueOf(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ModoCamara.APAGADA)

    private val materialId = savedStateHandle.getStateFlow(KEY_MATERIAL, RegistroRoute.SIN_MATERIAL)
    private val tipo = savedStateHandle.getStateFlow(KEY_TIPO, TipoMovimiento.COMPRA.name)
    private val fecha = savedStateHandle.getStateFlow(KEY_FECHA, System.currentTimeMillis())
    private val textoFactura = savedStateHandle.getStateFlow<String?>(KEY_TEXTO_FACTURA, null)
    private val intentoGuardar = savedStateHandle.getStateFlow(KEY_INTENTO, false)

    private val guardado = MutableStateFlow<UiState<Long>>(UiState.Idle)

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    // ---------- IA: el texto de cada fotograma se interpreta en Dispatchers.Default ----------

    private val lineasReconocidas = MutableStateFlow<List<String>>(emptyList())

    val deteccionFactura: StateFlow<DatosFactura?> = lineasReconocidas
        .map { lineas -> if (lineas.isEmpty()) null else extraerDatosFactura(lineas) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var ultimoCodigo: String? = null

    // ---------- Estado de la pantalla ----------

    private val usuario = authRepository.usuario
    private val obra = obraRepository.observarObraActiva()
    private val materiales = obra.flatMapLatest { o ->
        if (o == null) flowOf(emptyList()) else inventarioRepository.observarBodega(o.id)
    }

    private val formulario = combine(materialId, tipo, fecha, textoFactura) { id, t, f, texto ->
        FormularioRegistro(
            materialId = id.takeIf { it != RegistroRoute.SIN_MATERIAL },
            tipo = runCatching { TipoMovimiento.valueOf(t) }.getOrDefault(TipoMovimiento.COMPRA),
            fecha = f,
            textoFactura = texto
        )
    }

    private val datosBase = combine(obra, usuario, materiales) { o, u, lista -> Triple(o, u, lista) }

    val uiState: StateFlow<RegistroUiState> = combine(
        datosBase, formulario, combine(cantidad, costoTotal, proveedor) { a, b, c -> Triple(a, b, c) },
        intentoGuardar, guardado
    ) { (o, u, lista), form, (cant, costo, prov), intento, estadoGuardado ->
        // Un maestro de obra solo registra consumos: se corrige el tipo inicial
        val tipoValido = if (u != null && !Permisos.puedeRegistrar(u.rol, form.tipo)) TipoMovimiento.CONSUMO else form.tipo
        val f = form.copy(tipo = tipoValido)
        RegistroUiState(
            obra = o,
            usuario = u,
            materiales = lista,
            formulario = f,
            errores = erroresVisibles(o, u, lista, f, cant, costo, prov, intento),
            guardado = estadoGuardado
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RegistroUiState())

    // ---------- Eventos de la UI ----------

    fun onMaterialChange(id: Long) { savedStateHandle[KEY_MATERIAL] = id }
    fun onTipoChange(nuevo: TipoMovimiento) { savedStateHandle[KEY_TIPO] = nuevo.name }
    fun onCantidadChange(texto: String) { savedStateHandle[KEY_CANTIDAD] = soloDecimal(texto) }
    fun onCostoChange(texto: String) { savedStateHandle[KEY_COSTO] = soloDecimal(texto) }
    fun onProveedorChange(texto: String) { savedStateHandle[KEY_PROVEEDOR] = texto.take(80) }
    fun onFechaChange(millis: Long) { savedStateHandle[KEY_FECHA] = millis }

    fun abrirCamara(modo: ModoCamara) {
        lineasReconocidas.value = emptyList()
        ultimoCodigo = null
        savedStateHandle[KEY_CAMARA] = modo.name
    }

    fun cerrarCamara() {
        savedStateHandle[KEY_CAMARA] = ModoCamara.APAGADA.name
    }

    /** Llega desde el analizador de ML Kit, un fotograma a la vez. */
    fun onLineasReconocidas(lineas: List<String>) {
        lineasReconocidas.value = lineas
    }

    /** La persona confirma usar lo que la IA leyó; puede corregirlo antes de guardar. */
    fun usarDatosFactura() {
        val datos = deteccionFactura.value ?: return
        datos.proveedor?.let { savedStateHandle[KEY_PROVEEDOR] = it.take(80) }
        datos.totalCentavos?.let { savedStateHandle[KEY_COSTO] = centavosATexto(it) }
        datos.fecha?.let { savedStateHandle[KEY_FECHA] = it }
        savedStateHandle[KEY_TEXTO_FACTURA] = datos.textoCompleto.take(4000)
        savedStateHandle[KEY_TIPO] = TipoMovimiento.COMPRA.name
        cerrarCamara()
        _aviso.value = "Datos de la factura cargados. Revísalos antes de guardar."
    }

    /** Código leído: se busca el material de esta obra con ese código de barras. */
    fun onCodigoDetectado(codigo: String) {
        if (codigo == ultimoCodigo) return
        ultimoCodigo = codigo
        viewModelScope.launch {
            val lista = uiState.value.materiales
            val material = lista.firstOrNull { it.material.codigoBarras == codigo }
            if (material != null) {
                savedStateHandle[KEY_MATERIAL] = material.material.id
                cerrarCamara()
                _aviso.value = "Material identificado: ${material.material.nombre}"
            } else {
                _aviso.value = "El código $codigo no corresponde a ningún material de esta obra"
            }
        }
    }

    fun onAvisoMostrado() { _aviso.value = null }

    fun guardar() {
        savedStateHandle[KEY_INTENTO] = true
        viewModelScope.launch {
            val estado = uiState.first { it.obra != null && it.usuario != null }
            val resultado = registrarMovimiento.validar(
                entrada(estado.obra!!, estado.usuario!!, estado.materiales, estado.formulario,
                    cantidad.value, costoTotal.value, proveedor.value)
            )
            if (resultado is ResultadoValidacion.Valido) {
                guardado.value = UiState.Loading
                guardado.value = try {
                    val id = registrarMovimiento(resultado.movimiento) // Room en Dispatchers.IO
                    sincronizacion.programarSincronizacion()           // se envía apenas haya red
                    UiState.Success(id)
                } catch (e: Exception) {
                    UiState.Error(e.message ?: "No se pudo guardar")
                }
            }
        }
    }

    /** La pantalla llama a esto después de navegar, para no repetir la navegación. */
    fun onGuardadoConsumido() {
        guardado.value = UiState.Idle
        listOf(KEY_CANTIDAD, KEY_COSTO, KEY_PROVEEDOR).forEach { savedStateHandle[it] = "" }
        savedStateHandle[KEY_TEXTO_FACTURA] = null
        savedStateHandle[KEY_FECHA] = System.currentTimeMillis()
        savedStateHandle[KEY_INTENTO] = false
    }

    // ---------- Validación en tiempo real ----------

    private fun erroresVisibles(
        obra: Obra?, usuario: Usuario?, materiales: List<MaterialEnBodega>, form: FormularioRegistro,
        cant: String, costo: String, prov: String, intento: Boolean
    ): ErroresMovimiento {
        if (obra == null || usuario == null) return ErroresMovimiento()
        val resultado = registrarMovimiento.validar(entrada(obra, usuario, materiales, form, cant, costo, prov))
        val e = (resultado as? ResultadoValidacion.Invalido)?.errores ?: return ErroresMovimiento()
        // Un campo vacío solo se marca después del primer intento; un valor mal escrito, al instante.
        return ErroresMovimiento(
            tipo = e.tipo,
            material = e.material.takeIf { intento },
            cantidad = e.cantidad.takeIf { intento || cant.isNotBlank() },
            costo = e.costo.takeIf { intento || costo.isNotBlank() },
            proveedor = e.proveedor.takeIf { intento }
        )
    }

    private fun entrada(
        obra: Obra, usuario: Usuario, materiales: List<MaterialEnBodega>, form: FormularioRegistro,
        cant: String, costo: String, prov: String
    ) = EntradaMovimiento(
        obraId = obra.id,
        materialId = form.materialId,
        tipo = form.tipo,
        cantidadTexto = cant,
        costoTotalTexto = costo,
        proveedor = prov,
        existenciasDisponibles = materiales.firstOrNull { it.material.id == form.materialId }?.existencias ?: 0.0,
        fecha = form.fecha,
        rol = usuario.rol,
        usuarioId = usuario.id,
        textoFactura = form.textoFactura
    )

    /** Filtra al teclear: solo dígitos y un separador decimal. */
    private fun soloDecimal(texto: String): String {
        var separador = false
        return buildString {
            texto.forEach { c ->
                when {
                    c.isDigit() -> append(c)
                    (c == ',' || c == '.') && !separador -> { append(','); separador = true }
                }
            }
        }.take(12)
    }

    private fun centavosATexto(centavos: Long): String =
        NumberFormat.getNumberInstance(Locale.ROOT).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
            isGroupingUsed = false
        }.format(centavos / 100.0).replace('.', ',')

    private companion object {
        // materialId y tipo coinciden con los nombres de los argumentos de RegistroRoute
        const val KEY_MATERIAL = "materialId"
        const val KEY_TIPO = "tipo"
        const val KEY_CANTIDAD = "registro_cantidad"
        const val KEY_COSTO = "registro_costo"
        const val KEY_PROVEEDOR = "registro_proveedor"
        const val KEY_FECHA = "registro_fecha"
        const val KEY_TEXTO_FACTURA = "registro_texto_factura"
        const val KEY_INTENTO = "registro_intento"
        const val KEY_CAMARA = "registro_camara"
    }
}
