package ec.edu.puce.obrastock.data.ia

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executor
import kotlin.math.abs

/*
 * Edge AI con ML Kit: los modelos van incluidos en el APK (no se descargan) y la
 * inferencia corre en el teléfono, en hilos de fondo de ML Kit, sin enviar imágenes a
 * ningún servidor. CameraX entrega un fotograma a la vez (STRATEGY_KEEP_ONLY_LATEST):
 * mientras ML Kit procesa uno, los intermedios se descartan y la vista previa no se congela.
 * [ejecutor] solo recibe el resultado ya calculado; la interpretación del texto la hace el
 * ViewModel en Dispatchers.Default.
 */

/** Reconocimiento de texto (OCR) para leer facturas. */
class AnalizadorTexto(
    private val ejecutor: Executor,
    private val alReconocer: (List<String>) -> Unit
) : ImageAnalysis.Analyzer {

    private val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imagen: ImageProxy) {
        val media = imagen.image ?: run { imagen.close(); return }
        val entrada = InputImage.fromMediaImage(media, imagen.imageInfo.rotationDegrees)
        reconocedor.process(entrada)
            .addOnSuccessListener(ejecutor) { texto -> alReconocer(lineasPorFila(texto)) }
            .addOnCompleteListener(ejecutor) { imagen.close() }
    }

    fun cerrar() = reconocedor.close()

    /**
     * ML Kit agrupa el texto en bloques; en una factura "VALOR TOTAL" y "486,50" suelen
     * quedar en bloques distintos de la misma fila. Se reconstruyen filas por posición
     * vertical para que la etiqueta y el monto queden juntos.
     */
    private fun lineasPorFila(texto: Text): List<String> {
        val lineas = texto.textBlocks.flatMap { it.lines }.filter { it.boundingBox != null }
        val filas = mutableListOf<MutableList<Text.Line>>()
        lineas.sortedBy { it.boundingBox!!.centerY() }.forEach { linea ->
            val caja = linea.boundingBox!!
            val fila = filas.lastOrNull()
            val referencia = fila?.first()?.boundingBox
            if (fila != null && referencia != null && abs(referencia.centerY() - caja.centerY()) < caja.height() / 2) {
                fila.add(linea)
            } else {
                filas.add(mutableListOf(linea))
            }
        }
        return filas.map { fila -> fila.sortedBy { it.boundingBox!!.left }.joinToString(" ") { it.text } }
    }
}

/** Lectura de códigos de barras y QR para identificar materiales empacados. */
class AnalizadorCodigo(
    private val ejecutor: Executor,
    private val alDetectar: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val lector = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E, Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_QR_CODE
            )
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imagen: ImageProxy) {
        val media = imagen.image ?: run { imagen.close(); return }
        val entrada = InputImage.fromMediaImage(media, imagen.imageInfo.rotationDegrees)
        lector.process(entrada)
            .addOnSuccessListener(ejecutor) { codigos ->
                codigos.firstNotNullOfOrNull { it.rawValue?.takeIf { v -> v.isNotBlank() } }?.let(alDetectar)
            }
            .addOnCompleteListener(ejecutor) { imagen.close() }
    }

    fun cerrar() = lector.close()
}
