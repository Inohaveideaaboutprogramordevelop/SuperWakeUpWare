package com.superwakeupware.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.superwakeupware.R

// Drop "PressStart2P.ttf" into res/font/ for the 8-bit display numbers,
// and "Nunito-Bold.ttf" for rounded readable body text.
val PressStart2P = FontFamily(Font(R.font.press_start_2p))
val Nunito       = FontFamily(
    Font(R.font.nunito_bold,       FontWeight.Bold),
    Font(R.font.nunito_extrabold,  FontWeight.ExtraBold),
    Font(R.font.nunito_regular,    FontWeight.Normal),
)

val SuperWakeTypography = Typography(
    // Large countdown timer: pixel font, gold
    displayLarge  = TextStyle(fontFamily = PressStart2P, fontWeight = FontWeight.Normal,  fontSize = 64.sp, lineHeight = 72.sp),
    // Microgame instruction ("STOMP IT!")
    displayMedium = TextStyle(fontFamily = Nunito,       fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 44.sp),
    // Section title
    headlineLarge = TextStyle(fontFamily = Nunito,       fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 34.sp),
    // Alarm label, settings headers
    titleLarge    = TextStyle(fontFamily = Nunito,       fontWeight = FontWeight.Bold,      fontSize = 22.sp, lineHeight = 28.sp),
    // Body copy
    bodyLarge     = TextStyle(fontFamily = Nunito,       fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    // Small labels (lives counter, score)
    labelSmall    = TextStyle(fontFamily = PressStart2P, fontWeight = FontWeight.Normal,    fontSize = 10.sp, lineHeight = 16.sp),
)
