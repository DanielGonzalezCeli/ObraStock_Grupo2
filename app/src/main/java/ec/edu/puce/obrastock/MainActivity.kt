package ec.edu.puce.obrastock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ec.edu.puce.obrastock.ui.navigation.ObraStockNavGraph
import ec.edu.puce.obrastock.ui.theme.ObraStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObraStockTheme {
                ObraStockNavGraph()
            }
        }
    }
}
