
package cl.duoc.rinconsurapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.rinconsurapp.ui.theme.Exito

@Composable
fun DialogoCambiarContrasena(
    onCerrar: () -> Unit,
    onConfirmar: (
        String,
        String,
        (Boolean, String) -> Unit
    ) -> Unit,
    onExito: (String) -> Unit
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    var mostrarActual by remember { mutableStateOf(false) }
    var mostrarNueva by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val longitud = nueva.length >= 8
    val mayuscula = nueva.any { it.isUpperCase() }
    val numero = nueva.any { it.isDigit() }
    val especial = nueva.any {
        !it.isLetterOrDigit() && !it.isWhitespace()
    }

    val coinciden = nueva.isNotEmpty() && nueva == confirmar

    val formularioValido = actual.isNotBlank() &&
            longitud && mayuscula && numero && especial && coinciden

    AlertDialog(
        onDismissRequest = {
            if (!guardando) onCerrar()
        },
        title = {
            Text("Cambiar contraseña")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Ingresa tu contraseña actual y crea una nueva."
                )

                CampoContrasena(
                    valor = actual,
                    onValorCambia = {
                        actual = it
                        error = null
                    },
                    etiqueta = "Contraseña actual",
                    visible = mostrarActual,
                    onAlternarVisible = {
                        mostrarActual = !mostrarActual
                    }
                )

                CampoContrasena(
                    valor = nueva,
                    onValorCambia = {
                        nueva = it
                        error = null
                    },
                    etiqueta = "Nueva contraseña",
                    visible = mostrarNueva,
                    onAlternarVisible = {
                        mostrarNueva = !mostrarNueva
                    }
                )

                Text(
                    "Requisitos de contraseña",
                    style = MaterialTheme.typography.titleSmall
                )

                RequisitoClavePerfil(
                    "Mínimo 8 caracteres",
                    longitud
                )

                RequisitoClavePerfil(
                    "Al menos una mayúscula",
                    mayuscula
                )

                RequisitoClavePerfil(
                    "Al menos un número",
                    numero
                )

                RequisitoClavePerfil(
                    "Al menos un carácter especial",
                    especial
                )

                CampoContrasena(
                    valor = confirmar,
                    onValorCambia = {
                        confirmar = it
                        error = null
                    },
                    etiqueta = "Confirmar nueva contraseña",
                    visible = mostrarConfirmar,
                    onAlternarVisible = {
                        mostrarConfirmar = !mostrarConfirmar
                    },
                    error = if (
                        confirmar.isNotEmpty() && !coinciden
                    ) {
                        "Las contraseñas no coinciden."
                    } else {
                        null
                    }
                )

                error?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (guardando) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = formularioValido && !guardando,
                onClick = {
                    guardando = true
                    error = null

                    onConfirmar(actual, nueva) { exito, mensaje ->
                        guardando = false

                        if (exito) {
                            onExito(mensaje)
                            onCerrar()
                        } else {
                            error = mensaje
                        }
                    }
                }
            ) {
                Text("Cambiar contraseña")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !guardando,
                onClick = onCerrar
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RequisitoClavePerfil(
    texto: String,
    cumplido: Boolean
) {
    Text(
        text = "${if (cumplido) "✓" else "○"} $texto",
        style = MaterialTheme.typography.bodySmall,
        color = if (cumplido) {
            Exito
        } else {
            MaterialTheme.colorScheme.error
        }
    )
}
