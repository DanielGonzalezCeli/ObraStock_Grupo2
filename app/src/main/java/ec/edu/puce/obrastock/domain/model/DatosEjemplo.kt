package ec.edu.puce.obrastock.domain.model

/**
 * Datos de ejemplo para mostrar la pantalla Bodega en la Fase 1.
 * Se reemplazan por la base de datos (Room) en el commit de Samantha.
 */
object DatosEjemplo {

    val obra = Obra(id = 1, nombre = "Edificio Torres del Norte", direccion = "Av. 10 de Agosto, Quito")

    val materiales = listOf(
        Material(1, "CEM-001", "Cemento Portland 50 kg", "saco", 120.0, 30.0, obraId = 1),
        Material(2, "VAR-012", "Varilla corrugada 12 mm", "u", 45.0, 50.0, obraId = 1),
        Material(3, "ARE-001", "Arena fina", "m³", 8.5, 5.0, obraId = 1),
        Material(4, "BLO-015", "Bloque de hormigón 15 cm", "u", 900.0, 200.0, obraId = 1),
        Material(5, "ALA-016", "Alambre de amarre #16", "kg", 6.0, 10.0, obraId = 1),
    )

    val movimientos = listOf(
        Movimiento(1, materialId = 1, tipo = TipoMovimiento.ENTRADA, cantidad = 150.0, fechaMillis = 1_763_510_400_000),
        Movimiento(2, materialId = 1, tipo = TipoMovimiento.SALIDA, cantidad = 30.0, fechaMillis = 1_763_596_800_000),
        Movimiento(3, materialId = 2, tipo = TipoMovimiento.SALIDA, cantidad = 15.0, fechaMillis = 1_763_596_800_000),
    )
}
