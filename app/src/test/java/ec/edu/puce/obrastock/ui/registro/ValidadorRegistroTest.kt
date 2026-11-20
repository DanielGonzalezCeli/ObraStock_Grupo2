package ec.edu.puce.obrastock.ui.registro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorRegistroTest {

    private val valido = FormularioRegistro(
        codigo = "CEM-001",
        nombre = "Cemento Portland 50 kg",
        unidad = "saco",
        cantidad = "120",
    )

    @Test
    fun formularioCorrecto_noTieneErrores() {
        assertFalse(ValidadorRegistro.validar(valido).hayErrores)
    }

    @Test
    fun camposVacios_danError() {
        val errores = ValidadorRegistro.validar(FormularioRegistro())
        assertNotNull(errores.codigo)
        assertNotNull(errores.nombre)
        assertNotNull(errores.unidad)
        assertNotNull(errores.cantidad)
    }

    @Test
    fun cantidadConLetras_noRompeLaApp() {
        val errores = ValidadorRegistro.validar(valido.copy(cantidad = "abc"))
        assertEquals("Ingresa un número válido", errores.cantidad)
    }

    @Test
    fun cantidadCeroNegativaOEnorme_daError() {
        assertNotNull(ValidadorRegistro.validar(valido.copy(cantidad = "0")).cantidad)
        assertNotNull(ValidadorRegistro.validar(valido.copy(cantidad = "-5")).cantidad)
        assertNotNull(ValidadorRegistro.validar(valido.copy(cantidad = "999999999")).cantidad)
        assertNotNull(ValidadorRegistro.validar(valido.copy(cantidad = "NaN")).cantidad)
    }

    @Test
    fun cantidadConComa_seAcepta() {
        assertNull(ValidadorRegistro.validar(valido.copy(cantidad = "8,5")).cantidad)
        assertEquals(8.5, ValidadorRegistro.leerCantidad(" 8,5 ")!!, 0.0)
    }

    @Test
    fun codigoConEspaciosOSimbolos_daError() {
        assertNotNull(ValidadorRegistro.validar(valido.copy(codigo = "CE M")).codigo)
        assertNotNull(ValidadorRegistro.validar(valido.copy(codigo = "<script>")).codigo)
    }

    @Test
    fun nombreConCaracteresPeligrosos_daError() {
        assertNotNull(ValidadorRegistro.validar(valido.copy(nombre = "<b>Cemento</b>")).nombre)
    }

    @Test
    fun espaciosExtra_seLimpian() {
        assertEquals("Arena fina", ValidadorRegistro.limpiar("  Arena    fina  "))
        assertTrue(ValidadorRegistro.validar(valido.copy(nombre = "  Arena  fina ")).nombre == null)
    }
}
