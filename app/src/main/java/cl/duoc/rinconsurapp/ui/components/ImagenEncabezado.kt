package cl.duoc.rinconsurapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.SombraImagen

@Composable
fun ImagenEncabezado(
    @DrawableRes recursoImagen: Int? = null,
    descripcion: String,
    titulo: String,
    subtitulo: String,
    modificador: Modifier = Modifier
) {
    Box(
        modifier = modificador
            .fillMaxWidth()
            .height(220.dp)
            .background(Primario)
    ) {

        if (recursoImagen != null) {
            Image(
                painter = painterResource(id = recursoImagen),
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, SombraImagen)))
            )
        }


        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp, bottom = 40.dp)
        ) {
            Text(text = titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = subtitulo, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f))
        }
    }
}