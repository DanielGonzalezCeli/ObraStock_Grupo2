package ec.edu.puce.obrastock.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ObraDao {
    @Query("SELECT * FROM obras ORDER BY nombre")
    fun observarTodas(): Flow<List<ObraEntity>>

    @Upsert
    suspend fun guardar(obra: ObraEntity): Long
}

@Dao
interface MaterialDao {
    // Devuelve un Flow: la pantalla se actualiza sola cuando cambian los datos.
    @Query("SELECT * FROM materiales WHERE obraId = :obraId ORDER BY nombre")
    fun observarPorObra(obraId: Long): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materiales WHERE id = :id")
    suspend fun buscar(id: Long): MaterialEntity?

    @Upsert
    suspend fun guardar(material: MaterialEntity): Long

    @Query("DELETE FROM materiales WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Dao
interface MovimientoDao {
    @Query("SELECT * FROM movimientos WHERE materialId = :materialId ORDER BY fecha DESC")
    fun observarPorMaterial(materialId: Long): Flow<List<MovimientoEntity>>

    @Insert
    suspend fun registrar(movimiento: MovimientoEntity): Long
}
