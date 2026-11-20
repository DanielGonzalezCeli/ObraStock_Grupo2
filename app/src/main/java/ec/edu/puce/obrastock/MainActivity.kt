package ec.edu.puce.obrastock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ec.edu.puce.obrastock.ui.theme.ObraStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObraStockTheme {
                Scaffold { margenes ->
                    Text(
                        text = "ObraStock",
                        modifier = Modifier.padding(margenes).padding(16.dp)
                    )
                }
            }
        }
    }
}
