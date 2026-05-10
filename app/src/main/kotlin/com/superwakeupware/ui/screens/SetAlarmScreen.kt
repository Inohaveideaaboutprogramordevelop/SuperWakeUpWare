package com.superwakeupware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.superwakeupware.data.AlarmEntity
import com.superwakeupware.ui.theme.*

/**
 * The main "home" screen showing the list of alarms with a FAB to add new ones.
 */
@Composable
fun SetAlarmScreen(viewModel: SetAlarmViewModel = hiltViewModel()) {
    val alarms by viewModel.alarms.collectAsState(initial = emptyList())
    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick          = { showPicker = true },
                containerColor   = MarioRed,
                contentColor     = CloudWhite,
                shape            = RoundedCornerShape(16.dp),
            ) { Icon(Icons.Default.Add, contentDescription = "Add alarm") }
        },
        containerColor = BackgroundNight,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(BackgroundNight, SkyBlueDark))),
        ) {
            // Header
            Text(
                text      = "SUPER WAKE-UP WARE",
                style     = SuperWakeTypography.headlineLarge,
                color     = StarYellow,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 24.dp),
            )

            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm      = alarm,
                        onToggle   = { viewModel.toggleAlarm(alarm) },
                        onDelete   = { viewModel.deleteAlarm(alarm) },
                    )
                }
            }
        }
    }

    if (showPicker) {
        AlarmTimePicker(
            onDismiss = { showPicker = false },
            onConfirm = { hour, minute, label ->
                viewModel.addAlarm(hour, minute, label)
                showPicker = false
            },
        )
    }
}

@Composable
private fun AlarmCard(alarm: AlarmEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceCard),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text  = "%02d:%02d".format(alarm.hour, alarm.minute),
                    style = SuperWakeTypography.titleLarge,
                    color = if (alarm.enabled) CoinGold else OnSurface.copy(alpha = 0.4f),
                )
                if (alarm.label.isNotBlank()) {
                    Text(alarm.label, style = SuperWakeTypography.bodyLarge, color = OnSurface.copy(alpha = 0.7f))
                }
            }
            Switch(
                checked         = alarm.enabled,
                onCheckedChange = { onToggle() },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor      = CloudWhite,
                    checkedTrackColor      = PipeGreen,
                    uncheckedThumbColor    = OnSurface.copy(alpha = 0.4f),
                    uncheckedTrackColor    = SurfaceCard,
                ),
            )
        }
    }
}

@Composable
private fun AlarmTimePicker(onDismiss: () -> Unit, onConfirm: (Int, Int, String) -> Unit) {
    var hour   by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var label  by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceCard,
        title = { Text("New Alarm", style = SuperWakeTypography.titleLarge, color = StarYellow) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Simplified time input — a real implementation uses TimePickerDialog
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    NumberStepper(value = hour,   range = 0..23, onchange = { hour   = it }, label = "HH")
                    Text(":", style = SuperWakeTypography.headlineLarge, color = CoinGold)
                    NumberStepper(value = minute, range = 0..59, onchange = { minute = it }, label = "MM")
                }
                OutlinedTextField(
                    value         = label,
                    onValueChange = { label = it },
                    label         = { Text("Label (optional)", color = OnSurface.copy(alpha = 0.6f)) },
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = CoinGold,
                        unfocusedBorderColor = OnSurface.copy(alpha = 0.3f),
                        focusedTextColor     = OnSurface,
                        unfocusedTextColor   = OnSurface,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(hour, minute, label) },
                colors  = ButtonDefaults.buttonColors(containerColor = MarioRed),
            ) { Text("SET ALARM", style = SuperWakeTypography.labelSmall) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurface.copy(alpha = 0.6f)) }
        },
    )
}

@Composable
private fun NumberStepper(value: Int, range: IntRange, onchange: (Int) -> Unit, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = { if (value < range.last) onchange(value + 1) }) { Text("▲", color = CoinGold) }
        Text("%02d".format(value), style = SuperWakeTypography.headlineLarge, color = CloudWhite)
        TextButton(onClick = { if (value > range.first) onchange(value - 1) }) { Text("▼", color = CoinGold) }
        Text(label, style = SuperWakeTypography.labelSmall, color = OnSurface.copy(alpha = 0.5f))
    }
}
