package ec.edu.puce.obrastock.domain.usecase

import ec.edu.puce.obrastock.domain.model.DatosFactura
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToLong

/**
 * Reglas de extracción sobre el texto que reconoce ML Kit en una factura ecuatoriana.
 * Es Kotlin puro (se prueba sin cámara): recibe las líneas en orden de lectura y
 * devuelve proveedor, fecha y total. Lo que no encuentra queda en null para que la
 * persona lo complete; nunca se guarda sin su confirmación.
 */
class ExtraerDatosFacturaUseCase(
    private val zona: ZoneId = ZoneId.systemDefault()
) {

    operator fun invoke(lineas: List<String>): DatosFactura {
        val limpias = lineas.map { it.trim() }.filter { it.isNotEmpty() }
        return DatosFactura(
            proveedor = buscarProveedor(limpias),
            fecha = buscarFecha(limpias),
            totalCentavos = buscarTotal(limpias),
            textoCompleto = limpias.joinToString("\n")
        )
    }

    // ---------- Proveedor: el emisor va en las primeras líneas, en mayúsculas ----------

    /** Prefijos de palabras que indican que la línea NO es el nombre del emisor. */
    private val prefijosNoProveedor = listOf(
        "FACTURA", "RUC", "AUTORIZ", "DIREC", "MATRIZ", "SUCURSAL", "TELF", "TELEF", "FECHA",
        "OBLIGADO", "CONTRIBUYENTE", "AMBIENTE", "EMISI", "CLAVE", "ACCESO", "CLIENTE", "RAZ",
        "TOTAL", "SUBTOTAL", "IVA", "CORREO", "NUMERO", "NÚMERO"
    )
    private val regexPalabra = Regex("[A-ZÁÉÍÓÚÑÜ]+")

    private fun buscarProveedor(lineas: List<String>): String? =
        lineas.take(8).firstOrNull { linea ->
            val mayus = linea.uppercase()
            val letras = linea.count { it.isLetter() }
            val palabras = regexPalabra.findAll(mayus).map { it.value }.toList()
            letras >= 4 &&
                letras >= linea.length / 2 &&
                linea == mayus &&
                !mayus.contains("@") && !mayus.contains("R.U.C") && !mayus.contains("WWW") &&
                palabras.none { p -> prefijosNoProveedor.any { p.startsWith(it) } }
        }?.let { normalizarEspacios(it) }

    // ---------- Fecha: dd/mm/aaaa, dd-mm-aaaa o dd.mm.aa ----------

    private val regexFecha = Regex("""\b(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{2,4})\b""")

    private fun buscarFecha(lineas: List<String>): Long? {
        val conEtiqueta = lineas.filter { it.uppercase().contains("FECHA") }
        val candidatas = conEtiqueta + lineas
        for (linea in candidatas) {
            for (m in regexFecha.findAll(linea)) {
                val (d, mes, a) = m.destructured
                val anio = a.toInt().let { if (it < 100) 2000 + it else it }
                val fecha = runCatching { LocalDate.of(anio, mes.toInt(), d.toInt()) }.getOrNull() ?: continue
                if (anio in 2000..2100) {
                    // Mediodía local: evita que la zona horaria cambie el día
                    return fecha.atTime(12, 0).atZone(zona).toInstant().toEpochMilli()
                }
            }
        }
        return null
    }

    // ---------- Total: se prioriza "VALOR TOTAL", "TOTAL A PAGAR", "IMPORTE TOTAL" ----------

    private val regexMonto = Regex("""\d{1,3}(?:[.,\s]\d{3})*[.,]\d{2}(?!\d)|\d+[.,]\d{2}(?!\d)""")

    private val etiquetasTotal = listOf("VALOR TOTAL", "TOTAL A PAGAR", "IMPORTE TOTAL", "TOTAL")
    private val etiquetasExcluidas = listOf("SUBTOTAL", "SUB TOTAL", "TOTAL DESCUENTO", "TOTAL IVA", "TOTAL SIN")

    private fun buscarTotal(lineas: List<String>): Long? {
        for (etiqueta in etiquetasTotal) {
            val indices = lineas.indices.filter { i ->
                val mayus = lineas[i].uppercase()
                mayus.contains(etiqueta) && etiquetasExcluidas.none { mayus.contains(it) }
            }
            for (i in indices.reversed()) { // el total final suele ser el último
                val montos = (i..minOf(i + 2, lineas.lastIndex))
                    .asSequence()
                    .map { j -> montosEn(lineas[j]) }
                    .firstOrNull { it.isNotEmpty() }
                if (!montos.isNullOrEmpty()) return montos.max()
            }
        }
        // Sin etiqueta reconocible: el monto más alto es el candidato más probable
        return lineas.flatMap { montosEn(it) }.maxOrNull()
    }

    private fun montosEn(linea: String): List<Long> =
        regexMonto.findAll(linea).mapNotNull { aCentavos(it.value) }.toList()

    /** "1.234,56" / "1,234.56" / "486.50" / "486,50" → centavos. */
    internal fun aCentavos(texto: String): Long? {
        val limpio = texto.replace(" ", "")
        val decimal = limpio.indexOfLast { it == '.' || it == ',' }
        if (decimal < 0) return null
        val entero = limpio.substring(0, decimal).filter { it.isDigit() }
        val fraccion = limpio.substring(decimal + 1)
        val valor = "$entero.$fraccion".toDoubleOrNull() ?: return null
        return (valor * 100).roundToLong()
    }

    private fun normalizarEspacios(texto: String) = texto.replace(Regex("\\s+"), " ")
}
