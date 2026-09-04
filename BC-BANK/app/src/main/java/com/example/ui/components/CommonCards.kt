package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow

import com.example.ui.theme.BorderGlass
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.PrimaryVioletGlow
import com.example.ui.theme.SurfaceGlass

@Composable
fun DarkGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = BorderGlass,
    backgroundColor: Color = SurfaceCard,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.90f),
                        backgroundColor.copy(alpha = 0.70f)
                    )
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(18.dp)
    ) {
        content()
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color = PrimaryViolet,
    isPrimaryFilled: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isPrimaryFilled) {
                        Brush.linearGradient(
                            colors = listOf(
                                PrimaryViolet,
                                Color(0xFF5A45FF)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF161720),
                                Color(0xFF101017)
                            )
                        )
                    }
                )
                .border(
                    1.dp,
                    if (isPrimaryFilled) PrimaryVioletLight.copy(alpha = 0.4f) else BorderGlass,
                    RoundedCornerShape(18.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimaryFilled) Color.White else accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
fun ElegantProgressBar(
    progress: Float,
    barColor: Color = PrimaryViolet,
    gradientColors: List<Color>? = null,
    trackColor: Color = Color(0xFF1C1D26),
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "ProgressAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(trackColor)
    ) {
        val brush = remember(gradientColors, barColor) {
            if (gradientColors != null && gradientColors.size >= 2) {
                Brush.horizontalGradient(gradientColors)
            } else {
                Brush.horizontalGradient(
                    listOf(
                        barColor.copy(alpha = 0.8f),
                        barColor
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush)
        )
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "alimentación", "comida" -> Icons.Default.Fastfood
        "servicios", "luz", "agua" -> Icons.Default.Lightbulb
        "entretenimiento", "cine" -> Icons.Default.Movie
        "transporte", "pasajes" -> Icons.Default.DirectionsTransit
        "compras", "shopping" -> Icons.Default.ShoppingBag
        "salud", "farmacia" -> Icons.Default.HealthAndSafety
        "educación", "estudios" -> Icons.Default.School
        "sueldo", "ingreso" -> Icons.Default.MonetizationOn
        "ahorro", "meta" -> Icons.Default.Savings
        "transferencia" -> Icons.Default.Send
        "otros", "otro", "personalizado" -> Icons.Default.Category
        else -> Icons.Default.Payments
    }
}

fun getGoalCategoryIcon(iconName: String): ImageVector {
    return when (iconName.uppercase()) {
        "EMERGENCY" -> Icons.Default.Shield
        "TRAVEL" -> Icons.Default.CardTravel
        "TECH" -> Icons.Default.LaptopMac
        "HOME" -> Icons.Default.Home
        "CAR" -> Icons.Default.DirectionsCar
        "SAVINGS" -> Icons.Default.Savings
        "OTHER", "OTROS" -> Icons.Default.Category
        else -> Icons.Default.Savings
    }
}
