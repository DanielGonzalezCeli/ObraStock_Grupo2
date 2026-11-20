package ec.edu.puce.obrastock.ui.detalle

// Estados posibles de la sincronización con el servicio remoto.
// Al ser una interfaz sellada, el compilador obliga a atender todos los casos en un when.
sealed interface SyncUiState {
    data object Idle : SyncUiState
    data object Loading : SyncUiState
    data class Success(val mensaje: String) : SyncUiState
    data class Error(val mensaje: String) : SyncUiState
}
