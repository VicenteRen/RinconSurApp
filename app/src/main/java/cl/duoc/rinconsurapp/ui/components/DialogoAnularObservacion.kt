package cl.duoc.rinconsurapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun DialogoAnularObservacion(
    onCerrar: () -> Unit,
    onConfirmar: (
        String,
        String,
        (Boolean, String) -> Unit
    ) -> Unit,
    onExito: (String) -> Unit
) {
    var motivo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val motivoLimpio = motivo.trim()

    val motivoValido =
        motivoLimpio.length in 10..255 &&
                motivoLimpio.any { it.isLetter() } &&
                motivoLimpio.none { it.isISOControl() }

    val formularioValido =
        motivoValido && contrasena.isNotBlank()

    AlertDialog(
        onDismissRequest = {
            if (!enviando) onCerrar()
        },
        title = {
            Text("Anular observación")
        },
        text = {
            Column {
                Text(
                    "La observación se conservará en la base de datos, " +
                            "pero dejará de aparecer en los registros activos."
                )

                OutlinedTextField(
                    value = motivo,
                    onValueChange = {
                        if (it.length <= 255) {
                            motivo = it
                            error = ""
                        }
                    },
                    label = { Text("Motivo de anulación") },
                    placeholder = {
                        Text("Ej.: Registro duplicado por error")
                    },
                    supportingText = {
                        Text("${motivo.length}/255 caracteres (mínimo 10)")
                    },
                    isError = motivo.isNotEmpty() && !motivoValido,
                    minLines = 2,
                    maxLines = 4,
                    enabled = !enviando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        error = ""
                    },
                    label = { Text("Contraseña actual") },
                    visualTransformation =
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    enabled = !enviando,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotBlank()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (enviando) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = formularioValido && !enviando,
                onClick = {
                    enviando = true
                    error = ""

                    onConfirmar(
                        motivoLimpio,
                        contrasena
                    ) { correcto, mensaje ->
                        enviando = false

                        if (correcto) {
                            onExito(mensaje)
                            onCerrar()
                        } else {
                            error = mensaje
                        }
                    }
                }
            ) {
                Text("Confirmar anulación")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !enviando,
                onClick = onCerrar
            ) {
                Text("Cancelar")
            }
        }
    )
}