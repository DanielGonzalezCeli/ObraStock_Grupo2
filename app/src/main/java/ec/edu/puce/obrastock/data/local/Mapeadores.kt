package ec.edu.puce.obrastock.data.local

import ec.edu.puce.obrastock.domain.model.Material
import ec.edu.puce.obrastock.domain.model.Movimiento
import ec.edu.puce.obrastock.domain.model.Obra
import ec.edu.puce.obrastock.domain.model.TipoMovimiento

// Convierten entre las entidades de Room y los modelos del dominio.

fun ObraEntity.aDominio() = Obra(id = id, nombre = nombre, ubicacion = ubicacion)

fun Obra.aEntidad() = ObraEntity(id = id, nombre = nombre, ubicacion = ubicacion)

fun MaterialEntity.aDominio() = Material(
    id = id,
    obraId = obraId,
    nombre = nombre,
    categoria = categoria,
    unidad = unidad,
    existencias = existencias,
    existenciasMinimas = existenciasMinimas,
    costoUnitario = costoUnitario,
    descripcion = descripcion
)

fun Material.aEntidad() = MaterialEntity(
    id = id,
    obraId = obraId,
    nombre = nombre,
    categoria = categoria,
    unidad = unidad,
    existencias = existencias,
    existenciasMinimas = existenciasMinimas,
    costoUnitario = costoUnitario,
    descripcion = descripcion
)

fun MovimientoEntity.aDominio() = Movimiento(
    id = id,
    materialId = materialId,
    tipo = TipoMovimiento.valueOf(tipo),
    cantidad = cantidad,
    costoTotal = costoTotal,
    proveedor = proveedor,
    fecha = fecha
)

fun Movimiento.aEntidad() = MovimientoEntity(
    id = id,
    materialId = materialId,
    tipo = tipo.name,
    cantidad = cantidad,
    costoTotal = costoTotal,
    proveedor = proveedor,
    fecha = fecha
)
