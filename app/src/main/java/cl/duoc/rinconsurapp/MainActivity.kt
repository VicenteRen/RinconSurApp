package cl.duoc.rinconsurapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.rinconsurapp.ui.screens.InicioSesionContent
import cl.duoc.rinconsurapp.ui.screens.InicioSesionPreview
import cl.duoc.rinconsurapp.ui.screens.InicioSesionScreen
import cl.duoc.rinconsurapp.ui.screens.PerfilUsuarioScreen
import cl.duoc.rinconsurapp.ui.screens.RegistroObservacionScreen
import cl.duoc.rinconsurapp.ui.theme.RinconSurAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RinconSurAppTheme {
                RegistroObservacionScreen()
                //PerfilUsuarioScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RinconSurAppTheme {
        Greeting("Android")
    }
}