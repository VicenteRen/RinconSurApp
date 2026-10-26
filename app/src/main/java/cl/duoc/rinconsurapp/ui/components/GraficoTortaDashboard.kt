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
import androidx.compose.ui.unit.dp
import cl.duoc.rinconsurapp.ui.theme.Primario
import cl.duoc.rinconsurapp.ui.theme.Secundario
import cl.duoc.rinconsurapp.ui.theme.Acento
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import java.util.Locale
import androidx.compose.ui.res.stringResource
import cl.duoc.rinconsurapp.R


@Composable
fun GraficoTortaDashboard(
    visitas: Int,
    transito: Int,
    noDeterminado: Int
) {
    val total = visitas + transito + noDeterminado

    val colorVisitas = Primario
    val colorTransito = Secundario
    val colorNoDeterminado = Acento

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
        Text(stringResource(R.string.grafico_torta_sin_datos))
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

            val categorias = listOf(
                Triple(
                    stringResource(R.string.dashboard_visitas),
                    visitas,
                    colorVisitas
                ),
                Triple(
                    stringResource(R.string.dashboard_transito),
                    transito,
                    colorTransito
                ),
                Triple(
                    stringResource(R.string.dashboard_no_determinado),
                    noDeterminado,
                    colorNoDeterminado
                )
            )

            categorias.forEach { (nombre, cantidad, color) ->
                val porcentaje = cantidad.toFloat() / total * 100f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color)
                    )

                    Text(
                        text = nombre,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "$cantidad (${
                            String.format(
                                Locale.forLanguageTag("es-CL"),
                                "%.1f",
                                porcentaje
                            )
                        } %)"
                    )
                }
            }
        }
    }
}