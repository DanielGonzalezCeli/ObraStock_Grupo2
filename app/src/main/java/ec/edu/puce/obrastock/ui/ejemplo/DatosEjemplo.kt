package ec.edu.puce.obrastock.ui.ejemplo

import ec.edu.puce.obrastock.domain.model.Material
import ec.edu.puce.obrastock.domain.model.Movimiento
import ec.edu.puce.obrastock.domain.model.Obra
import ec.edu.puce.obrastock.domain.model.TipoMovimiento

// Datos inventados solo para dibujar las pantallas de la Fase 1.
// No son precios ni existencias reales; en la Fase 2 se reemplazan por los datos de Room.
object DatosEjemplo {

    val obra = Obra(id = 1L, nombre = "Obra de ejemplo", ubicacion = "Quito")

    val materiales = listOf(
        Material(1L, 1L, "Cemento", "Aglomerantes", "saco", 12.0, 20.0, 8.0, "Saco de 50 kg"),
        Material(2L, 1L, "Varilla corrugada 12 mm", "Acero", "unidad", 80.0, 40.0, 11.0),
        Material(3L, 1L, "Arena fina", "Áridos", "m³", 6.0, 3.0, 20.0),
        Material(4L, 1L, "Ladrillo mambrón", "Mampostería", "unidad", 450.0, 500.0, 0.3),
        Material(5L, 1L, "Tubería PVC 4 pulgadas", "Plomería", "unidad", 15.0, 8.0, 9.0),
        Material(6L, 1L, "Pintura blanca", "Acabados", "galón", 4.0, 6.0, 25.0)
    )

    val movimientos = listOf(
        Movimiento(1L, 1L, TipoMovimiento.COMPRA, 30.0, 240.0, "Ferretería de ejemplo", 1_700_000_000_000L),
        Movimiento(2L, 1L, TipoMovimiento.CONSUMO, 18.0, null, null, 1_700_500_000_000L),
        Movimiento(3L, 2L, TipoMovimiento.COMPRA, 100.0, 1100.0, "Ferretería de ejemplo", 1_700_000_000_000L)
    )

    fun buscarMaterial(id: Long): Material? = materiales.firstOrNull { it.id == id }

    fun movimientosDe(materialId: Long): List<Movimiento> =
        movimientos.filter { it.materialId == materialId }
}
