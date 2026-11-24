package ec.edu.puce.obrastock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.puce.obrastock.domain.model.Usuario
import ec.edu.puce.obrastock.domain.repository.AuthRepository
import ec.edu.puce.obrastock.domain.repository.PreferenciasRepository
import ec.edu.puce.obrastock.domain.repository.SincronizacionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la sesión: decide si se muestra el login o la app. */
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val usuario: Usuario) : EstadoSesion
}

/**
 * ViewModel de la Activity: sesión y preferencia de color. Al iniciar con sesión,
 * sincroniza con el servidor para traer lo que otros registraron.
 */
class AppViewModel(
    authRepository: AuthRepository,
    preferencias: PreferenciasRepository,
    private val sincronizacion: SincronizacionRepository
) : ViewModel() {

    val sesion: StateFlow<EstadoSesion> = authRepository.usuario
        .map { u -> if (u == null) EstadoSesion.SinSesion else EstadoSesion.Activa(u) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    val colorDinamico: StateFlow<Boolean> =
        preferencias.colorDinamico.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            // Una sincronización por cada inicio de sesión (al abrir la app o al entrar)
            authRepository.usuario.map { it?.id }.distinctUntilChanged().collect { id ->
                if (id != null) sincronizacion.programarSincronizacion()
            }
        }
    }
}
