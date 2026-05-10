package com.superwakeupware.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SuperWakeColorScheme = darkColorScheme(
    primary         = MarioRed,
    onPrimary       = CloudWhite,
    primaryContainer    = MarioRedDark,
    secondary       = CoinGold,
    onSecondary     = BobOmbBlack,
    secondaryContainer  = CoinGoldDark,
    tertiary        = PipeGreen,
    onTertiary      = CloudWhite,
    background      = BackgroundNight,
    onBackground    = OnSurface,
    surface         = SurfaceCard,
    onSurface       = OnSurface,
    error           = DangerRed,
)

@Composable
fun SuperWakeUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SuperWakeColorScheme,
        typography  = SuperWakeTypography,
        content     = content,
    )
}
