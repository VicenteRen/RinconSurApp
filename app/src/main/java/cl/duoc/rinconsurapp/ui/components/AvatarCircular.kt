package cl.duoc.rinconsurapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun AvatarCircular(
    @DrawableRes recursoImagen: Int,
    descripcion: String,
    modificador: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = recursoImagen),
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modificador
            .size(120.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
    )
}