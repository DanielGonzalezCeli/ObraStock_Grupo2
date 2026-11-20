package ec.edu.puce.obrastock.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Las claves primarias se generan solas (autoGenerate). El tipo de movimiento se guarda como texto.

@Entity(tableName = "obras")
data class ObraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val ubicacion: String
)

@Entity(
    tableName = "materiales",
    foreignKeys = [
        ForeignKey(
            entity = ObraEntity::class,
            parentColumns = ["id"],
            childColumns = ["obraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("obraId")]
)
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val obraId: Long,
    val nombre: String,
    val categoria: String,
    val unidad: String,
    val existencias: Double,
    val existenciasMinimas: Double,
    val costoUnitario: Double,
    val descripcion: String? = null
)

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("materialId")]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialId: Long,
    val tipo: String,
    val cantidad: Double,
    val costoTotal: Double? = null,
    val proveedor: String? = null,
    val fecha: Long
)
