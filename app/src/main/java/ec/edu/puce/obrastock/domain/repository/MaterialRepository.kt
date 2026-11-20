package ec.edu.puce.obrastock.domain.repository

import ec.edu.puce.obrastock.domain.model.Material
import ec.edu.puce.obrastock.domain.model.Movimiento
import kotlinx.coroutines.flow.Flow

// Contrato que usa la interfaz. No conoce Room ni Retrofit.
// La implementación se escribe en la Fase 2: Room como fuente de verdad y Retrofit para sincronizar.
interface MaterialRepository {
    fun observarMateriales(obraId: Long): Flow<List<Material>>
    suspend fun guardar(material: Material)
    suspend fun registrarMovimiento(movimiento: Movimiento)
    suspend fun sincronizar()
}
