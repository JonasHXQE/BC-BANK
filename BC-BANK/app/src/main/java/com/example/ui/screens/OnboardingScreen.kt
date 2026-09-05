package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class OnboardingStep(val stepNumber: Int, val title: String) {
    ACCOUNT_TYPE(1, "Tipo de Cuenta"),
    PROFILE_DATA(2, "Datos del Perfil"),
    SUMMARY(3, "Resumen y Apertura")
}

private data class AccountOption(
    val id: String,
    val name: String,
    val badge: String,
    val description: String,
    val icon: ImageVector,
    val benefits: List<String>
)

@Composable
fun OnboardingScreen(
    userEmail: String,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onCompleteOnboarding: (
        accountType: String,
        fullName: String,
        dni: String,
        phone: String,
        pin: String,
        isBusiness: Boolean,
        businessName: String,
        businessRuc: String,
        birthDate: String,
        isKid: Boolean,
        age: Int
    ) -> Unit,
    onLogout: () -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.ACCOUNT_TYPE) }

    // Account Selection
    var selectedAccountType by remember { mutableStateOf("Cuenta de Ahorros BC-BANK") }

    // Profile Form State
    var fullName by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isBusiness by remember { mutableStateOf(false) }
    var businessName by remember { mutableStateOf("") }
    var businessRuc by remember { mutableStateOf("") }
    var acceptedSummaryTerms by remember { mutableStateOf(true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val accountOptions = remember {
        listOf(
            AccountOption(
                id = "Cuenta de Ahorros BC-BANK",
                name = "Cuenta Digital Ahorros BC-BANK",
                badge = "Cero Comisiones",
                description = "Tu cuenta digital para administrar tus fondos con total disponibilidad y seguridad.",
                icon = Icons.Default.AccountBalance,
                benefits = listOf(
                    "S/ 0 mantenimiento y sin saldo mínimo requerido",
                    "Disponibilidad inmediata de tus fondos 24/7",
                    "Transferencias gratuitas a cualquier banco e interoperables",
                    "Tarjeta de débito digital Visa emitida al instante"
                )
            ),
            AccountOption(
                id = "Cuenta Sueldo BC-BANK",
                name = "Cuenta Sueldo / Nómina BC-BANK",
                badge = "Mayores Beneficios",
                description = "Diseñada para recibir tus pagos y haberes con total disponibilidad y sin comisiones.",
                icon = Icons.Default.Payments,
                benefits = listOf(
                    "Retiros 100% gratuitos e ilimitados en cualquier cajero",
                    "Recepción directa de tus ingresos o nómina",
                    "Transferencias inmediatas sin comisiones",
                    "Notificaciones de abono en tiempo real sin costo"
                )
            ),
            AccountOption(
                id = "Cuenta Negocio BC-BANK",
                name = "Cuenta Negocio / Emprendedor BC-BANK",
                badge = "Empresarial",
                description = "Potencia las operaciones de tu negocio con herramientas de cobro y registro de RUC.",
                icon = Icons.Default.Store,
                benefits = listOf(
                    "Acepta pagos con QR interoperable y transferencias",
                    "Registro opcional de Razón Social y RUC 10 / 20",
                    "Emisión y validación de comprobantes bancarios al instante",
                    "Límites diarios de transferencias extendidos"
                )
            )
        )
    }

    BackHandler {
        when (currentStep) {
            OnboardingStep.SUMMARY -> {
                currentStep = OnboardingStep.PROFILE_DATA
                validationError = null
            }
            OnboardingStep.PROFILE_DATA -> {
                currentStep = OnboardingStep.ACCOUNT_TYPE
                validationError = null
            }
            OnboardingStep.ACCOUNT_TYPE -> {
                onLogout()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 540.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo & Stepper
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryViolet, PrimaryVioletDark)
                        )
                    )
                    .border(1.5.dp, BorderGlass, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "BC-BANK",
                    tint = AccentGold,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Apertura de Cuenta Bancaria",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sigue los pasos oficiales para abrir y activar tu cuenta en BC-BANK",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // STEP PROGRESS INDICATOR
            StepperIndicator(
                currentStep = currentStep
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Error display
            val displayError = errorMessage ?: validationError
            AnimatedVisibility(visible = displayError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1214)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = displayError ?: "",
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // MULTI-STEP CONTENT
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.stepNumber > initialState.stepNumber) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "OnboardingStepAnimation"
            ) { step ->
                when (step) {
                    OnboardingStep.ACCOUNT_TYPE -> {
                        StepAccountTypeContent(
                            accountOptions = accountOptions,
                            selectedAccount = selectedAccountType,
                            onSelectAccount = { option ->
                                selectedAccountType = option.id
                                if (option.id == "Cuenta Negocio BC-BANK") {
                                    isBusiness = true
                                }
                                validationError = null
                            },
                            onNext = {
                                currentStep = OnboardingStep.PROFILE_DATA
                            },
                            onLogout = onLogout
                        )
                    }

                    OnboardingStep.PROFILE_DATA -> {
                        StepProfileDataContent(
                            fullName = fullName,
                            onFullNameChange = { fullName = it; validationError = null },
                            dni = dni,
                            onDniChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) { dni = it; validationError = null } },
                            phone = phone,
                            onPhoneChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) { phone = it; validationError = null } },
                            pin = pin,
                            onPinChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { pin = it; validationError = null } },
                            confirmPin = confirmPin,
                            onConfirmPinChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { confirmPin = it; validationError = null } },
                            isBusiness = isBusiness,
                            onBusinessChange = { isBusiness = it },
                            businessName = businessName,
                            onBusinessNameChange = { businessName = it; validationError = null },
                            businessRuc = businessRuc,
                            onBusinessRucChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) { businessRuc = it; validationError = null } },
                            onBack = {
                                currentStep = OnboardingStep.ACCOUNT_TYPE
                                validationError = null
                            },
                            onNext = {
                                when {
                                    fullName.trim().length < 3 -> validationError = "Ingresa tus nombres y apellidos completos"
                                    dni.trim().length != 8 -> validationError = "El DNI debe tener 8 dígitos numéricos exactos"
                                    phone.trim().length != 9 -> validationError = "El teléfono debe tener 9 dígitos numéricos exactos"
                                    pin.trim().length != 6 -> validationError = "El PIN de seguridad debe tener exactamente 6 dígitos numéricos"
                                    pin != confirmPin -> validationError = "Los dos PIN ingresados no coinciden"
                                    isBusiness && businessName.trim().isBlank() -> validationError = "Ingresa la Razón Social o Nombre Comercial"
                                    isBusiness && businessRuc.trim().length != 11 -> validationError = "El RUC de la empresa debe tener 11 dígitos exactos"
                                    else -> {
                                        validationError = null
                                        currentStep = OnboardingStep.SUMMARY
                                    }
                                }
                            }
                        )
                    }

                    OnboardingStep.SUMMARY -> {
                        StepSummaryContent(
                            userEmail = userEmail,
                            accountType = selectedAccountType,
                            fullName = fullName.trim(),
                            dni = dni.trim(),
                            phone = phone.trim(),
                            isBusiness = isBusiness,
                            businessName = businessName.trim(),
                            businessRuc = businessRuc.trim(),
                            acceptedTerms = acceptedSummaryTerms,
                            onAcceptedTermsChange = { acceptedSummaryTerms = it },
                            isLoading = isLoading,
                            onBack = {
                                currentStep = OnboardingStep.PROFILE_DATA
                                validationError = null
                            },
                            onSubmit = {
                                if (!acceptedSummaryTerms) {
                                    validationError = "Debes aceptar los Términos y Declaración Jurada para abrir tu cuenta"
                                    return@StepSummaryContent
                                }
                                validationError = null
                                onCompleteOnboarding(
                                    selectedAccountType,
                                    fullName.trim(),
                                    dni.trim(),
                                    phone.trim(),
                                    pin.trim(),
                                    isBusiness,
                                    businessName.trim(),
                                    businessRuc.trim(),
                                    "",
                                    false,
                                    25
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperIndicator(currentStep: OnboardingStep) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepCircle(
                number = 1,
                label = "Tipo de Cuenta",
                isActive = currentStep.stepNumber >= 1,
                isCompleted = currentStep.stepNumber > 1
            )
            StepDivider(isCompleted = currentStep.stepNumber > 1)
            StepCircle(
                number = 2,
                label = "Perfil",
                isActive = currentStep.stepNumber >= 2,
                isCompleted = currentStep.stepNumber > 2
            )
            StepDivider(isCompleted = currentStep.stepNumber > 2)
            StepCircle(
                number = 3,
                label = "Resumen",
                isActive = currentStep.stepNumber >= 3,
                isCompleted = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val progress = when (currentStep) {
            OnboardingStep.ACCOUNT_TYPE -> 0.33f
            OnboardingStep.PROFILE_DATA -> 0.66f
            OnboardingStep.SUMMARY -> 1.0f
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = EmeraldPrimary,
            trackColor = SurfaceDark
        )
    }
}

@Composable
private fun StepCircle(number: Int, label: String, isActive: Boolean, isCompleted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> EmeraldPrimary
                        isActive -> EmeraldPrimary.copy(alpha = 0.2f)
                        else -> SurfaceDark
                    }
                )
                .border(
                    1.5.dp,
                    if (isActive || isCompleted) EmeraldPrimary else BorderDark,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "$number",
                    color = if (isActive) EmeraldPrimary else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive || isCompleted) TextPrimary else TextMuted,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun StepDivider(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(2.dp)
            .background(if (isCompleted) EmeraldPrimary else BorderDark)
    )
}

// -----------------------------------------------------------------------------
// STEP 1: SELECCIONAR TIPO DE CUENTA
// -----------------------------------------------------------------------------
@Composable
private fun StepAccountTypeContent(
    accountOptions: List<AccountOption>,
    selectedAccount: String,
    onSelectAccount: (AccountOption) -> Unit,
    onNext: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Paso 1: Selecciona tu Tipo de Cuenta",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Elige la cuenta que mejor se adapte a tus finanzas. Podrás solicitar tarjetas adicionales luego:",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        accountOptions.forEach { option ->
            val isSelected = selectedAccount == option.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectAccount(option) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF0F261F) else SurfaceCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) EmeraldPrimary else BorderGlass
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) EmeraldPrimary.copy(alpha = 0.2f)
                                        else SurfaceDark
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) EmeraldPrimary else AccentGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = option.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = option.badge,
                                    color = if (isSelected) EmeraldLight else AccentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(
                                    2.dp,
                                    if (isSelected) EmeraldPrimary else BorderDark,
                                    CircleShape
                                )
                                .background(if (isSelected) EmeraldPrimary else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = option.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    HorizontalDivider(color = BorderDark.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(8.dp))

                    option.benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = benefit,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("onboarding_step1_next_btn")
        ) {
            Text("Continuar a Datos del Perfil", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cerrar Sesión", color = TextMuted, fontSize = 13.sp)
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 2: COMPLETAR PERFIL Y PIN
// -----------------------------------------------------------------------------
@Composable
private fun StepProfileDataContent(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    dni: String,
    onDniChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    confirmPin: String,
    onConfirmPinChange: (String) -> Unit,
    isBusiness: Boolean,
    onBusinessChange: (Boolean) -> Unit,
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    businessRuc: String,
    onBusinessRucChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Paso 2: Datos Oficiales y Seguridad",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ingresa tus datos conforme a tu documento de identidad y configura tu clave transaccional:",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text("Nombres y Apellidos Completos", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // DNI Field
        OutlinedTextField(
            value = dni,
            onValueChange = onDniChange,
            label = { Text("DNI (8 dígitos numéricos)", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_dni_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone Field
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono Móvil (9 dígitos)", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_phone_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 6-digit PIN Field
        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            label = { Text("PIN de Seguridad (6 dígitos)", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_pin_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm 6-digit PIN Field
        OutlinedTextField(
            value = confirmPin,
            onValueChange = onConfirmPinChange,
            label = { Text("Confirmar PIN de Seguridad", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_confirm_pin_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Business Account Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("¿Deseas registrar un Negocio o RUC?", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Activa Razón Social y RUC de 11 dígitos", color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
                checked = isBusiness,
                onCheckedChange = onBusinessChange,
                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
            )
        }

        if (isBusiness) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = businessName,
                onValueChange = onBusinessNameChange,
                label = { Text("Razón Social o Nombre Comercial", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = EmeraldPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = businessRuc,
                onValueChange = onBusinessRucChange,
                label = { Text("RUC (11 dígitos exactos)", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Atrás", color = TextSecondary, fontSize = 14.sp)
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1.5f)
                    .height(50.dp)
                    .testTag("onboarding_step2_next_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ver Resumen", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 3: RESUMEN Y CONFIRMACIÓN
// -----------------------------------------------------------------------------
@Composable
private fun StepSummaryContent(
    userEmail: String,
    accountType: String,
    fullName: String,
    dni: String,
    phone: String,
    isBusiness: Boolean,
    businessName: String,
    businessRuc: String,
    acceptedTerms: Boolean,
    onAcceptedTermsChange: (Boolean) -> Unit,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Paso 3: Resumen de tu Cuenta",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Verifica que tus datos sean exactos antes de proceder a la apertura formal en BC-BANK:",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Premium Official Bank Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderGlass)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SOLICITUD DE APERTURA",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRE-APROBADO",
                            color = EmeraldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SummaryRow(label = "Tipo de Cuenta:", value = accountType, isHighlight = true)
                SummaryRow(label = "Titular:", value = fullName)
                SummaryRow(label = "DNI:", value = dni)
                SummaryRow(label = "Teléfono:", value = "+51 $phone")
                SummaryRow(label = "Correo Vinculado:", value = userEmail.ifBlank { "Correo de acceso" })

                if (isBusiness) {
                    SummaryRow(label = "Razón Social:", value = businessName)
                    SummaryRow(label = "RUC:", value = businessRuc)
                }

                SummaryRow(label = "PIN de Seguridad:", value = "•••••• (Configurado)", isHighlight = false)
                SummaryRow(label = "Saldo Inicial de Cuenta:", value = "S/ 0.00", isHighlight = false)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terms acceptance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptedTerms,
                onCheckedChange = onAcceptedTermsChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldPrimary,
                    uncheckedColor = TextMuted,
                    checkmarkColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Declaro que los datos ingresados son verídicos y acepto el Contrato de Cuentas Digitales y Políticas de BC-BANK.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Modificar", color = TextSecondary, fontSize = 13.sp)
            }

            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1.8f)
                    .height(52.dp)
                    .testTag("onboarding_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir Cuenta e Ingresar", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = if (isHighlight) EmeraldLight else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 240.dp)
        )
    }
}
