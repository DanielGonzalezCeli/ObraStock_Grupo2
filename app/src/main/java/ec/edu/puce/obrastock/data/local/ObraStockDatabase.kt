package ec.edu.puce.obrastock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ObraEntity::class, MaterialEntity::class, MovimientoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ObraStockDatabase : RoomDatabase() {

    abstract fun obraDao(): ObraDao
    abstract fun materialDao(): MaterialDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile
        private var instancia: ObraStockDatabase? = null

        // Una sola instancia compartida por toda la aplicación.
        fun obtener(contexto: Context): ObraStockDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    ObraStockDatabase::class.java,
                    "obrastock.db"
                ).build().also { instancia = it }
            }
    }
}
