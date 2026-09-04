package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletDark
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun AuthScreen(
    savedDni: String = "",
    savedEmail: String = "",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onLogin: (documentOrEmail: String, pass: String, remember: Boolean) -> Unit,
    onRegister: (email: String, pass: String) -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onForgotPassword: (email: String) -> Unit = {}
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    val focusManager = LocalFocusManager.current

    // Login Form State
    var loginInput by remember { mutableStateOf(savedDni.ifEmpty { savedEmail }) }
    var loginPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    // Register Form State (Simplified)
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(true) }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var formValidationNotice by remember { mutableStateOf<String?>(null) }
    var isGoogleConnecting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            isGoogleConnecting = false
        }
    }

    BackHandler {
        if (authMode == AuthMode.REGISTER) {
            authMode = AuthMode.LOGIN
            formValidationNotice = null
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000L) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(
                    context,
                    "Presiona atrás una vez más para salir de la aplicación",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x306D5BFF), Color(0x1038BDF8), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(250f, 150f),
                    radius = 850f
                )
            )
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("auth_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Banking Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                EmeraldPrimary.copy(alpha = 0.8f),
                                AccentGold.copy(alpha = 0.5f)
                            )
                        ),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_bcbank_logo),
                    contentDescription = "BC-BANK Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "BC-BANK",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Banca Móvil & Pagos Digitales",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector (Login / Abrir Cuenta)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF101117))
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                // Tab Login
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (authMode == AuthMode.LOGIN) PrimaryViolet else Color.Transparent
                        )
                        .clickable {
                            authMode = AuthMode.LOGIN
                            formValidationNotice = null
                        }
                        .padding(vertical = 12.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        color = if (authMode == AuthMode.LOGIN) Color.White else TextMuted,
                        fontSize = 14.sp,
                        fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Tab Register
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (authMode == AuthMode.REGISTER) PrimaryViolet else Color.Transparent
                        )
                        .clickable {
                            authMode = AuthMode.REGISTER
                            formValidationNotice = null
                        }
                        .padding(vertical = 12.dp)
                        .testTag("tab_register"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Abrir Cuenta",
                        color = if (authMode == AuthMode.REGISTER) Color.White else TextMuted,
                        fontSize = 14.sp,
                        fontWeight = if (authMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Error or Validation Message
            val currentError = errorMessage ?: formValidationNotice
            AnimatedVisibility(visible = currentError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = currentError ?: "",
                        color = ExpenseRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Tab Content
            AnimatedContent(
                targetState = authMode,
                transitionSpec = {
                    if (targetState == AuthMode.REGISTER) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "auth_content"
            ) { mode ->
                if (mode == AuthMode.LOGIN) {
                    // LOGIN FORM
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Acceso a Banca Segura",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // DNI or Email Input
                                OutlinedTextField(
                                    value = loginInput,
                                    onValueChange = { loginInput = it; formValidationNotice = null },
                                    label = { Text("Número de DNI / RUC o Correo") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = EmeraldLight
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = authTextFieldColors(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_login_user")
                                )

                                // Password
                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it; formValidationNotice = null },
                                    label = { Text("Contraseña de Acceso") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = EmeraldLight
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                                tint = TextMuted
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (loginInput.isNotBlank() && loginPassword.isNotBlank()) {
                                                onLogin(loginInput, loginPassword, rememberSession)
                                            }
                                        }
                                    ),
                                    colors = authTextFieldColors(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_login_password")
                                )

                                // Remember & Forgot Password
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { rememberSession = !rememberSession }
                                    ) {
                                        Checkbox(
                                            checked = rememberSession,
                                            onCheckedChange = { rememberSession = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = EmeraldPrimary,
                                                uncheckedColor = TextMuted,
                                                checkmarkColor = Color.White
                                            )
                                        )
                                        Text(
                                            text = "Recordar datos",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Text(
                                        text = "¿Olvidaste tu clave?",
                                        color = EmeraldLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clickable { showForgotPasswordDialog = true }
                                            .testTag("btn_forgot_password")
                                    )
                                }

                                // Login Button
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        if (loginInput.isBlank()) {
                                            formValidationNotice = "Por favor ingresa tu DNI, RUC o Correo Electrónico."
                                            return@Button
                                        }
                                        if (loginPassword.isBlank()) {
                                            formValidationNotice = "Por favor ingresa tu contraseña de acceso."
                                            return@Button
                                        }
                                        formValidationNotice = null
                                        onLogin(loginInput.trim(), loginPassword.trim(), rememberSession)
                                    },
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_submit_login")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Iniciar Sesión",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Divider & Google Sign-In
                                GoogleAuthSection(
                                    isLoading = isLoading || isGoogleConnecting,
                                    onGoogleClick = {
                                        isGoogleConnecting = true
                                        onGoogleSignIn()
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // REGISTER FORM (Simplified: Email + Password + Confirm Password)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Crear Cuenta BC-BANK",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Ingresa tu correo y define tu contraseña de acceso. Tras verificar tu correo completarás los datos de tu cuenta.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                // Email Input
                                OutlinedTextField(
                                    value = registerEmail,
                                    onValueChange = { registerEmail = it; formValidationNotice = null },
                                    label = { Text("Correo Electrónico") },
                                    placeholder = { Text("tu.nombre@gmail.com") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = EmeraldLight
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = authTextFieldColors(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_register_email")
                                )

                                // Password Input
                                OutlinedTextField(
                                    value = registerPassword,
                                    onValueChange = { 
                                        if (it.length <= 20) {
                                            registerPassword = it
                                            formValidationNotice = null
                                        }
                                    },
                                    label = { Text("Contraseña (8 a 20 caracteres)") },
                                    placeholder = { Text("Letras, números o caracteres") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = EmeraldLight
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { registerPasswordVisible = !registerPasswordVisible }) {
                                            Icon(
                                                imageVector = if (registerPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (registerPasswordVisible) "Ocultar" else "Mostrar",
                                                tint = TextMuted
                                            )
                                        }
                                    },
                                    visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = authTextFieldColors(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_register_password")
                                )

                                // Confirm Password Input
                                OutlinedTextField(
                                    value = registerConfirmPassword,
                                    onValueChange = { 
                                        if (it.length <= 20) {
                                            registerConfirmPassword = it
                                            formValidationNotice = null
                                        }
                                    },
                                    label = { Text("Confirmar Contraseña") },
                                    placeholder = { Text("Repite tu contraseña") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = EmeraldLight
                                        )
                                    },
                                    visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    colors = authTextFieldColors(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_register_confirm_password")
                                )

                                // Terms Checkbox
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = acceptedTerms,
                                        onCheckedChange = { acceptedTerms = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = EmeraldPrimary,
                                            uncheckedColor = TextMuted,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Text(
                                        text = "Acepto los Términos, Condiciones y Política de Privacidad de BC-BANK.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }

                                // Register Submit Button
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        val email = registerEmail.trim()
                                        val pass = registerPassword.trim()
                                        val confirmPass = registerConfirmPassword.trim()

                                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                            formValidationNotice = "Por favor ingresa un correo electrónico válido."
                                            return@Button
                                        }
                                        if (pass.length < 8 || pass.length > 20) {
                                            formValidationNotice = "La contraseña debe tener entre 8 y 20 caracteres."
                                            return@Button
                                        }
                                        if (pass != confirmPass) {
                                            formValidationNotice = "Las contraseñas no coinciden. Verifícalas."
                                            return@Button
                                        }
                                        if (!acceptedTerms) {
                                            formValidationNotice = "Debes aceptar los Términos y Condiciones para continuar."
                                            return@Button
                                        }

                                        formValidationNotice = null
                                        onRegister(email, pass)
                                    },
                                    enabled = !isLoading && !isGoogleConnecting,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_submit_register")
                                ) {
                                    if (isLoading && !isGoogleConnecting) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Crear Cuenta BC-BANK",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Divider & Google Sign-In
                                GoogleAuthSection(
                                    isLoading = isLoading || isGoogleConnecting,
                                    onGoogleClick = {
                                        isGoogleConnecting = true
                                        onGoogleSignIn()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Google Loading Modal Dialog
        if (isGoogleConnecting) {
            Dialog(
                onDismissRequest = { /* Modal persists during active Google request */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldPrimary,
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Conectando con Google",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Iniciando servicio de autenticación segura...",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = "Recuperación de Contraseña",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Ingresa tu correo electrónico registrado para enviarte el enlace de restablecimiento seguro.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = forgotPasswordEmail,
                            onValueChange = { forgotPasswordEmail = it },
                            label = { Text("Correo Electrónico") },
                            placeholder = { Text("ejemplo@correo.com") },
                            singleLine = true,
                            colors = authTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (forgotPasswordEmail.isNotBlank()) {
                                onForgotPassword(forgotPasswordEmail.trim())
                                showForgotPasswordDialog = false
                                Toast.makeText(context, "Enlace de recuperación enviado si el correo está registrado", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Enviar Enlace", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancelar", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun GoogleAuthSection(
    isLoading: Boolean,
    onGoogleClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDark)
            Text(
                text = "o continúa con",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDark)
        }

        OutlinedButton(
            onClick = onGoogleClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SurfaceDark,
                contentColor = TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_google_signin")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Google "G" logo representation
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4285F4)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continuar con Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EmeraldPrimary,
    unfocusedBorderColor = BorderDark,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = EmeraldLight,
    unfocusedLabelColor = TextMuted,
    cursorColor = EmeraldLight,
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark
)
