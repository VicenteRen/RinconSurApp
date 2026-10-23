package cl.duoc.rinconsurapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GraficoBarrasDashboard(
    datos: Map<String, Int>
) {
    val progreso = remember(datos) { Animatable(0f) }

    LaunchedEffect(datos) {
        progreso.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    val maximo = (datos.values.maxOrNull() ?: 0).coerceAtLeast(1)

    val colorBarra = MaterialTheme.colorScheme.primary
    val colorFondo = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        datos.forEach { (nombre, cantidad) ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(text = cantidad.toString())
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                ) {
                    val anchoBarra =
                        size.width * cantidad.toFloat() / maximo * progreso.value

                    drawRoundRect(
                        color = colorFondo,
                        size = size,
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )

                    if (cantidad > 0) {
                        drawRoundRect(
                            color = colorBarra,
                            topLeft = Offset.Zero,
                            size = Size(anchoBarra, size.height),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}