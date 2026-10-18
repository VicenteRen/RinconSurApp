package cl.duoc.rinconsurapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GraficoTortaDashboard(
    visitas: Int,
    transito: Int,
    noDeterminado: Int
) {
    val total = visitas + transito + noDeterminado

    val colorVisitas = Color(0xFF9575CD)
    val colorTransito = Color(0xFF4DB6AC)
    val colorNoDeterminado = Color(0xFFFFB74D)

    val progreso = remember(visitas, transito, noDeterminado) {
        Animatable(0f)
    }

    LaunchedEffect(visitas, transito, noDeterminado) {
        progreso.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    if (total == 0) {
        Text("Sin datos para mostrar")
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val diametro = minOf(size.width, size.height)

                val esquina = Offset(
                    (size.width - diametro) / 2f,
                    (size.height - diametro) / 2f
                )

                val tamano = Size(diametro, diametro)

                val valores = listOf(visitas, transito, noDeterminado)
                val colores = listOf(
                    colorVisitas,
                    colorTransito,
                    colorNoDeterminado
                )

                var inicio = -90f

                valores.forEachIndexed { indice, cantidad ->
                    val angulo = cantidad.toFloat() / total * 360f

                    drawArc(
                        color = colores[indice],
                        startAngle = inicio,
                        sweepAngle = angulo * progreso.value,
                        useCenter = true,
                        topLeft = esquina,
                        size = tamano
                    )

                    inicio += angulo
                }
            }

            Text("Visitas: $visitas")
            Text("Tránsito: $transito")
            Text("No determinado: $noDeterminado")
        }
    }
}