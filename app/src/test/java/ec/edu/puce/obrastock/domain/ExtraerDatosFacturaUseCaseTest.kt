package ec.edu.puce.obrastock.domain

import ec.edu.puce.obrastock.domain.usecase.ExtraerDatosFacturaUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ExtraerDatosFacturaUseCaseTest {

    private val zona = ZoneId.of("America/Guayaquil")
    private val extraer = ExtraerDatosFacturaUseCase(zona)

    private fun fecha(millis: Long?): LocalDate? =
        millis?.let { Instant.ofEpochMilli(it).atZone(zona).toLocalDate() }

    @Test
    fun `factura tipica del SRI`() {
        val datos = extraer(
            listOf(
                "FERRETERIA EL CONSTRUCTOR",
                "R.U.C.: 1792345678001",
                "FACTURA No. 001-002-000012345",
                "Dirección Matriz: Av. 10 de Agosto N45-12",
                "Fecha Emisión: 12/10/2026",
                "Razón Social / Nombres y Apellidos: Juan Pérez",
                "Cemento Holcim 50kg 40 8,50 340,00",
                "SUBTOTAL 12% 423,04",
                "IVA 15% 63,46",
                "VALOR TOTAL",
                "486,50"
            )
        )
        assertEquals("FERRETERIA EL CONSTRUCTOR", datos.proveedor)
        assertEquals(LocalDate.of(2026, 10, 12), fecha(datos.fecha))
        assertEquals(48_650L, datos.totalCentavos)
    }

    @Test
    fun `total con punto decimal y miles`() {
        val datos = extraer(listOf("DISENSA NORTE", "SUBTOTAL 1,100.00", "TOTAL A PAGAR $ 1,265.00"))
        assertEquals(126_500L, datos.totalCentavos)
    }

    @Test
    fun `no confunde subtotal con total`() {
        val datos = extraer(listOf("ACEROS DEL PICHINCHA", "SUBTOTAL 800,00", "TOTAL 920,00"))
        assertEquals(92_000L, datos.totalCentavos)
    }

    @Test
    fun `texto sin datos devuelve nulos`() {
        val datos = extraer(listOf("hola", "sin números"))
        assertNull(datos.proveedor)
        assertNull(datos.fecha)
        assertNull(datos.totalCentavos)
    }

    @Test
    fun `conversion de montos`() {
        assertEquals(123_456L, extraer.aCentavos("1.234,56"))
        assertEquals(123_456L, extraer.aCentavos("1,234.56"))
        assertEquals(48_650L, extraer.aCentavos("486.50"))
    }
}
