package ec.edu.puce.obrastock.ui.registro

/** Lo que el usuario escribe en el formulario; todo llega como texto. */
data class FormularioRegistro(
    val codigo: String = "",
    val nombre: String = "",
    val unidad: String = "",
    val cantidad: String = "",
)

/** Un mensaje de error por campo; null significa que el campo está bien. */
data class ErroresRegistro(
    val codigo: String? = null,
    val nombre: String? = null,
    val unidad: String? = null,
    val cantidad: String? = null,
) {
    val hayErrores: Boolean
        get() = listOf(codigo, nombre, unidad, cantidad).any { it != null }
}

/**
 * Validación segura del formulario: nunca lanza excepciones con datos raros
 * (texto vacío, letras en la cantidad, números enormes, NaN, caracteres extraños).
 */
object ValidadorRegistro {

    const val MAX_NOMBRE = 60
    const val MAX_CANTIDAD = 100_000.0
    val UNIDADES = listOf("u", "kg", "m", "m²", "m³", "saco")

    private val patronCodigo = Regex("^[A-Za-z0-9-]{3,20}$")
    private val caracteresNoPermitidos = Regex("[<>{};\"\\\\]")

    /** Quita espacios al inicio y al final y junta los espacios repetidos. */
    fun limpiar(texto: String): String = texto.trim().replace(Regex("\\s+"), " ")

    /** Convierte la cantidad aceptando coma o punto; devuelve null si no es un número válido. */
    fun leerCantidad(texto: String): Double? =
        limpiar(texto).replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

    fun validar(formulario: FormularioRegistro): ErroresRegistro {
        val codigo = limpiar(formulario.codigo)
        val nombre = limpiar(formulario.nombre)
        val cantidadTexto = limpiar(formulario.cantidad)
        val cantidad = leerCantidad(cantidadTexto)

        return ErroresRegistro(
            codigo = when {
                codigo.isEmpty() -> "El código es obligatorio"
                !patronCodigo.matches(codigo) -> "Usa de 3 a 20 letras, números o guiones"
                else -> null
            },
            nombre = when {
                nombre.isEmpty() -> "El nombre es obligatorio"
                nombre.length > MAX_NOMBRE -> "Máximo $MAX_NOMBRE caracteres"
                caracteresNoPermitidos.containsMatchIn(nombre) -> "El nombre tiene caracteres no permitidos"
                else -> null
            },
            unidad = if (formulario.unidad !in UNIDADES) "Elige una unidad" else null,
            cantidad = when {
                cantidadTexto.isEmpty() -> "La cantidad es obligatoria"
                cantidad == null -> "Ingresa un número válido"
                cantidad <= 0.0 -> "La cantidad debe ser mayor que cero"
                cantidad > MAX_CANTIDAD -> "La cantidad es demasiado grande"
                else -> null
            },
        )
    }
}
