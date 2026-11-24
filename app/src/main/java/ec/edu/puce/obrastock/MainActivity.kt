package ec.edu.puce.obrastock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ec.edu.puce.obrastock.ui.AppViewModel
import ec.edu.puce.obrastock.ui.common.AppViewModelFactory
import ec.edu.puce.obrastock.ui.navigation.ObraStockAppRoot
import ec.edu.puce.obrastock.ui.theme.ObraStockTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels { AppViewModelFactory.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: la app dibuja detrás de las barras del sistema y gestiona los insets.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val colorDinamico by appViewModel.colorDinamico.collectAsStateWithLifecycle()
            ObraStockTheme(dynamicColor = colorDinamico) {
                ObraStockAppRoot(appViewModel)
            }
        }
    }
}
