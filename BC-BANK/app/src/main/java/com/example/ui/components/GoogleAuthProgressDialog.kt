package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firebase.UserCloudData
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class GoogleAuthState {
    object Idle : GoogleAuthState()
    data class Processing(val step: String = "Iniciando autenticación segura con Google...") : GoogleAuthState()
    data class Verifying(val email: String) : GoogleAuthState()
    data class RequirePin(
        val uid: String,
        val email: String,
        val displayName: String,
        val expectedPin: String,
        val cloudData: UserCloudData?
    ) : GoogleAuthState()
    data class Success(val message: String = "Autenticación Exitosa • Bienvenido") : GoogleAuthState()
    data class Cancelled(val message: String = "Proceso cancelado o cerrado por el usuario.") : GoogleAuthState()
    data class Error(val message: String) : GoogleAuthState()
}

@Composable
fun GoogleAuthProgressDialog(
    state: GoogleAuthState,
    onDismiss: () -> Unit,
    onVerifyPin: (enteredPin: String) -> Unit = {}
) {
    if (state is GoogleAuthState.Idle) return

    val isDismissible = state is GoogleAuthState.Error || state is GoogleAuthState.Cancelled || state is GoogleAuthState.RequirePin

    var pinInput by remember(state) { mutableStateOf("") }
    var pinError by remember(state) { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            if (isDismissible) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = isDismissible,
            dismissOnClickOutside = isDismissible,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                when (state) {
                    is GoogleAuthState.Error -> ExpenseRed.copy(alpha = 0.5f)
                    is GoogleAuthState.Cancelled -> GoldAccent.copy(alpha = 0.5f)
                    is GoogleAuthState.Success -> EmeraldPrimary.copy(alpha = 0.6f)
                    else -> BorderDark
                }
            ),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "google_auth_anim"
                ) { target ->
                    when (target) {
                        is GoogleAuthState.Processing -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceCard)
                                        .border(1.dp, BorderDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = EmeraldPrimary,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Text(
                                    text = target.step,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Conectando de forma cifrada con Google Identity Services...",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is GoogleAuthState.Verifying -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = EmeraldLight,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Text(
                                    text = "Verificando Cuenta Bancaria",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Validando identidad y sincronizando credenciales para ${target.email}...",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is GoogleAuthState.RequirePin -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.2f))
                                        .border(2.dp, EmeraldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ingresa tu PIN de Seguridad",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = target.email,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = EmeraldLight,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    text = "Esta cuenta ya está registrada en el sistema. Por seguridad, introduce tu PIN bancario de 6 dígitos para ingresar:",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )

                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = { input ->
                                        if (input.length <= 6 && input.all { it.isDigit() }) {
                                            pinInput = input
                                            pinError = null
                                        }
                                    },
                                    placeholder = {
                                        Text("••••••", color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                    },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            val cleanInput = pinInput.trim()
                                            val expected = target.expectedPin.trim().replace(".0", "")
                                            val isMatch = expected.isBlank() ||
                                                    cleanInput == expected ||
                                                    cleanInput.padStart(6, '0') == expected.padStart(6, '0') ||
                                                    cleanInput == expected.take(6) ||
                                                    (cleanInput.toIntOrNull() != null && cleanInput.toIntOrNull() == expected.toDoubleOrNull()?.toInt())

                                            if (isMatch) {
                                                onVerifyPin(cleanInput)
                                            } else {
                                                pinError = "PIN incorrecto. Ingresa tu clave de 6 dígitos configurada previamente."
                                            }
                                        }
                                    ),
                                    singleLine = true,
                                    isError = pinError != null,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = BorderDark,
                                        errorBorderColor = ExpenseRed,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = BackgroundDark,
                                        unfocusedContainerColor = BackgroundDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("google_auth_pin_input")
                                )

                                if (pinError != null) {
                                    Text(
                                        text = pinError ?: "",
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onDismiss,
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("google_auth_pin_cancel_button")
                                    ) {
                                        Text("Cancelar", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            val cleanInput = pinInput.trim()
                                            val expected = target.expectedPin.trim().replace(".0", "")
                                            val isMatch = expected.isBlank() ||
                                                    cleanInput == expected ||
                                                    cleanInput.padStart(6, '0') == expected.padStart(6, '0') ||
                                                    cleanInput == expected.take(6) ||
                                                    (cleanInput.toIntOrNull() != null && cleanInput.toIntOrNull() == expected.toDoubleOrNull()?.toInt())

                                            if (isMatch) {
                                                onVerifyPin(cleanInput)
                                            } else {
                                                pinError = "PIN incorrecto. Ingresa tu clave de 6 dígitos configurada previamente."
                                            }
                                        },
                                        enabled = pinInput.length == 6,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldPrimary,
                                            contentColor = Color.Black,
                                            disabledContainerColor = EmeraldPrimary.copy(alpha = 0.3f),
                                            disabledContentColor = Color.Black.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(44.dp)
                                            .testTag("google_auth_pin_confirm_button")
                                    ) {
                                        Text("Acceder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        is GoogleAuthState.Success -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.2f))
                                        .border(2.dp, EmeraldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                                Text(
                                    text = target.message,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Sesión cifrada iniciada correctamente.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is GoogleAuthState.Cancelled -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.15f))
                                        .border(2.dp, GoldAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                                Text(
                                    text = "Acceso con Google Cancelado",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = target.message,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldAccent.copy(alpha = 0.2f),
                                        contentColor = GoldAccent
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Text("Entendido / Cerrar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                        is GoogleAuthState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed.copy(alpha = 0.15f))
                                        .border(2.dp, ExpenseRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                                Text(
                                    text = "Error de Autenticación",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = target.message,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text("Entendido", color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
