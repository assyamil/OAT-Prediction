package com.example.oatprediction.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.oatprediction.R

val JakartaSans = FontFamily(
    Font(R.font.jakarta_regular, FontWeight.Normal),
    Font(R.font.jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.jakarta_bold, FontWeight.Bold),
)
private fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) =
    TextStyle(fontFamily = JakartaSans, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight, letterSpacing = 0.sp)
val Typography = Typography(
    displayLarge = style(40, 48, FontWeight.Bold), displayMedium = style(36, 44, FontWeight.Bold), displaySmall = style(32, 40, FontWeight.Bold),
    headlineLarge = style(28, 36, FontWeight.Bold), headlineMedium = style(24, 32, FontWeight.Bold), headlineSmall = style(20, 28, FontWeight.SemiBold),
    titleLarge = style(20, 28, FontWeight.Bold), titleMedium = style(16, 24, FontWeight.SemiBold), titleSmall = style(14, 20, FontWeight.SemiBold),
    bodyLarge = style(16, 24), bodyMedium = style(14, 22), bodySmall = style(12, 18),
    labelLarge = style(14, 20, FontWeight.Bold), labelMedium = style(12, 18, FontWeight.SemiBold), labelSmall = style(11, 16, FontWeight.SemiBold),
)
