package ec.edu.puce.obrastock.ui.bodega

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.puce.obrastock.domain.model.MaterialEnBodega
import ec.edu.puce.obrastock.domain.model.Obra
import ec.edu.puce.obrastock.domain.model.Rol
import ec.edu.puce.obrastock.domain.repository.AuthRepository
import ec.edu.puce.obrastock.domain.repository.InventarioRepository
import ec.edu.puce.obrastock.domain.repository.ObraRepository
import ec.edu.puce.obrastock.domain.repository.SincronizacionRepository
import ec.edu.puce.obrastock.domain.usecase.FiltrarMaterialesUseCase
import ec.edu.puce.obrastock.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BodegaUiState(
    val obras: List<Obra> = emptyList(),
    val obraActiva: Obra? = null,
    val categorias: List<String> = emptyList(),
    val categoriaSeleccionada: String? = null,
    val materiales: UiState<List<MaterialEnBodega>> = UiState.Loading,
    val totalEnBodega: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class BodegaViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val obraRepository: ObraRepository,
    inventarioRepository: InventarioRepository,
    authRepository: AuthRepository,
    private val sincronizacion: SincronizacionRepository,
    private val filtrarMateriales: FiltrarMaterialesUseCase
) : ViewModel() {

    /** Texto de búsqueda: vive en SavedStateHandle para sobrevivir a la muerte del proceso. */
    val consulta: StateFlow<String> = savedStateHandle.getStateFlow(KEY_CONSULTA, "")
    private val categoria: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_CATEGORIA, null)

    val rol: StateFlow<Rol?> = authRepository.usuario
        .map { it?.rol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val pendientes: StateFlow<Int> = inventarioRepository.contarPendientes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _sincronizacion = MutableStateFlow<UiState<String>>(UiState.Idle)
    val sincronizacionEstado: StateFlow<UiState<String>> = _sincronizacion.asStateFlow()

    /** Aviso persistente "Sin conexión · últimos datos" (diapositiva 12 del diseño). */
    private val _sinConexion = MutableStateFlow<String?>(null)
    val sinConexion: StateFlow<String?> = _sinConexion.asStateFlow()

    /** Material encontrado al escanear su código: la pantalla abre su ficha. */
    private val _materialEscaneado = MutableStateFlow<MaterialEnBodega?>(null)
    val materialEscaneado: StateFlow<MaterialEnBodega?> = _materialEscaneado.asStateFlow()

    private val obraActiva = obraRepository.observarObraActiva()

    private val bodega = obraActiva.flatMapLatest { obra ->
        if (obra == null) flowOf(emptyList()) else inventarioRepository.observarBodega(obra.id)
    }

    val uiState: StateFlow<BodegaUiState> = combine(
        obraRepository.observarObras(),
        obraActiva,
        bodega,
        consulta,
        categoria
    ) { obras, obra, lista, texto, cat ->
        BodegaUiState(
            obras = obras,
            obraActiva = obra,
            categorias = lista.map { it.material.categoria }.distinct().sorted(),
            categoriaSeleccionada = cat,
            materiales = UiState.Success(filtrarMateriales(lista, texto, cat)),
            totalEnBodega = lista.size
        )
    }
        .flowOn(Dispatchers.Default) // filtrado y ordenamiento fuera del hilo principal
        .catch { e -> emit(BodegaUiState(materiales = UiState.Error(e.message ?: "No se pudo cargar la bodega"))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BodegaUiState())

    fun onConsultaChange(texto: String) {
        savedStateHandle[KEY_CONSULTA] = texto
    }

    fun onCategoriaChange(categoria: String?) {
        savedStateHandle[KEY_CATEGORIA] = categoria
    }

    fun onObraChange(obraId: Long) {
        viewModelScope.launch { obraRepository.seleccionarObra(obraId) }
        savedStateHandle[KEY_CATEGORIA] = null
    }

    init {
        // Al abrir la bodega se trae lo último del servidor sin interrumpir a la persona
        sincronizar(silencioso = true)
    }

    /** Deslizar hacia abajo: envía lo pendiente y trae lo nuevo del servidor. */
    fun sincronizar(silencioso: Boolean = false) {
        if (_sincronizacion.value is UiState.Loading) return
        viewModelScope.launch {
            if (!silencioso) _sincronizacion.value = UiState.Loading
            val resultado = sincronizacion.sincronizar()
            _sinConexion.value = resultado.exceptionOrNull()?.message
            _sincronizacion.value = resultado.fold(
                onSuccess = { r ->
                    when {
                        r.enviados > 0 -> UiState.Success("Sincronizado: ${r.enviados} movimientos enviados")
                        silencioso -> UiState.Idle
                        else -> UiState.Success("Todo al día")
                    }
                },
                onFailure = { if (silencioso) UiState.Idle else UiState.Error(it.message ?: "No se pudo sincronizar") }
            )
        }
    }

    /** Código de barras leído con ML Kit: se busca el material en la bodega de la obra. */
    fun onCodigoEscaneado(codigo: String) {
        viewModelScope.launch {
            val material = bodega.first().firstOrNull { it.material.codigoBarras == codigo }
            if (material != null) _materialEscaneado.value = material
            else _sincronizacion.value = UiState.Error("El código $codigo no está en la bodega de esta obra")
        }
    }

    fun onMaterialEscaneadoAbierto() {
        _materialEscaneado.value = null
    }

    fun onMensajeMostrado() {
        _sincronizacion.value = UiState.Idle
    }

    private companion object {
        const val KEY_CONSULTA = "bodega_consulta"
        const val KEY_CATEGORIA = "bodega_categoria"
    }
}
