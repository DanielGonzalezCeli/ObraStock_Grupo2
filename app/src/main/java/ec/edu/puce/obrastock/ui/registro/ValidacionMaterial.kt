package ec.edu.puce.obrastock.ui.registro

// Lee un número escrito por una persona sin que la aplicación se caiga.
// Acepta coma o punto decimal y rechaza texto, vacíos, negativos y valores no numéricos.
object ValidacionMaterial {

    fun leerNumero(texto: String): Double? {
        val limpio = texto.trim().replace(',', '.')
        val numero = limpio.toDoubleOrNull() ?: return null
        if (numero.isNaN() || numero.isInfinite() || numero < 0.0) return null
        return numero
    }
}
