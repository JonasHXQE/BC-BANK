package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography =
  Typography(
    displayLarge = TextStyle(
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Bold,
      fontSize = 38.sp,
      lineHeight = 44.sp,
      letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 20.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.sp
    ),
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
  )
