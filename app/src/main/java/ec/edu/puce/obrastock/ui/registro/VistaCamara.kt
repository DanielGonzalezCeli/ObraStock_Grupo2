package ec.edu.puce.obrastock.ui.registro

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import ec.edu.puce.obrastock.data.ia.AnalizadorCodigo
import ec.edu.puce.obrastock.data.ia.AnalizadorTexto
import java.util.concurrent.Executors

/**
 * Vista previa de CameraX con el analizador de ML Kit que corresponda al modo.
 * La cámara se enlaza al ciclo de vida de la pantalla: se apaga sola al salir o al
 * pasar a segundo plano, y se libera por completo al cerrar el panel.
 */
@Composable
fun VistaCamara(
    modo: ModoCamara,
    onLineas: (List<String>) -> Unit,
    onCodigo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val alLeerLineas by rememberUpdatedState(onLineas)
    val alLeerCodigo by rememberUpdatedState(onCodigo)

    val vistaPrevia = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(modo, lifecycleOwner) {
        // Hilo de fondo para entregar fotogramas al analizador (nunca el hilo principal)
        val hiloAnalisis = Executors.newSingleThreadExecutor()
        val hiloPrincipal = ContextCompat.getMainExecutor(context)
        val analizador = when (modo) {
            ModoCamara.FACTURA -> AnalizadorTexto(hiloPrincipal) { alLeerLineas(it) }
            ModoCamara.CODIGO -> AnalizadorCodigo(hiloPrincipal) { alLeerCodigo(it) }
            ModoCamara.APAGADA -> null
        }
        val futuro = ProcessCameraProvider.getInstance(context)
        var proveedor: ProcessCameraProvider? = null

        if (analizador != null) {
            futuro.addListener({
                val p = futuro.get()
                proveedor = p
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(vistaPrevia.surfaceProvider) }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // descarta intermedios
                    .build()
                    .also { it.setAnalyzer(hiloAnalisis, analizador) }
                runCatching {
                    p.unbindAll()
                    p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis)
                }
            }, hiloPrincipal)
        }

        onDispose {
            proveedor?.unbindAll()
            when (analizador) {
                is AnalizadorTexto -> analizador.cerrar()
                is AnalizadorCodigo -> analizador.cerrar()
                else -> Unit
            }
            hiloAnalisis.shutdown()
        }
    }

    AndroidView(factory = { vistaPrevia }, modifier = modifier)
}
