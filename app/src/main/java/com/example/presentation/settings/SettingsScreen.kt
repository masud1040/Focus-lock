package com.example.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datastore.ThemeMode
import com.example.data.datastore.UserPreferences
import com.example.domain.engine.UsageStatsHelper
import com.example.domain.repository.AppBlockerRepository
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalBorderSubtle
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900

@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    repository: AppBlockerRepository,
    onToggleProtection: (Boolean) -> Unit,
    onSetLockSettingsWithPin: (Boolean) -> Unit,
    onSetEmergencyUnlockEnabled: (Boolean) -> Unit,
    onSetEmergencyUnlockDuration: (Int) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit,
    onResetAllData: () -> Unit
) {
    val context = LocalContext.current
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showEmergencyDurationDialog by remember { mutableStateOf(false) }
    var showTamperInfoDialog by remember { mutableStateOf(false) }

    val hasUsageAccess = remember { UsageStatsHelper.hasUsageStatsPermission(context) }
    val isBatteryOptimized = remember {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings & Security",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Configure protection, PIN security, and system permissions",
                fontSize = 13.sp,
                color = Zinc500
            )
        }

        // Section: Protection Controls
        item {
            SettingsSectionHeader(title = "PROTECTION CONTROLS")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsSwitchRow(
                        title = "Protection Active",
                        subtitle = "Master switch for all blocking rules & schedules",
                        checked = userPreferences.isProtectionActive,
                        onCheckedChange = onToggleProtection
                    )

                    SettingsSwitchRow(
                        title = "Lock Settings Behind PIN",
                        subtitle = "Require PIN to alter blocked apps or schedules",
                        checked = userPreferences.isLockSettingsWithPin,
                        enabled = userPreferences.isPinSet,
                        onCheckedChange = onSetLockSettingsWithPin
                    )

                    SettingsSwitchRow(
                        title = "Allow Emergency Unlocks",
                        subtitle = "Allows temporary unblocking with PIN",
                        checked = userPreferences.isEmergencyUnlockEnabled,
                        onCheckedChange = onSetEmergencyUnlockEnabled
                    )

                    if (userPreferences.isEmergencyUnlockEnabled) {
                        SettingsClickableRow(
                            title = "Emergency Unlock Duration",
                            value = "${userPreferences.emergencyUnlockDurationMinutes} minutes",
                            onClick = { showEmergencyDurationDialog = true }
                        )
                    }
                }
            }
        }

        // Section: Security & PIN
        item {
            SettingsSectionHeader(title = "PIN SECURITY")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (userPreferences.isPinSet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Security PIN Active", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Text("Secured with SHA-256 + salt", fontSize = 12.sp, color = EmeraldSuccess)
                            }
                            Button(
                                onClick = { showSetPinDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A), contentColor = CyanAccent)
                            ) {
                                Text("Change", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        TextButton(
                            onClick = onRemovePin,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Remove PIN", color = Color(0xFFF43F5E), fontSize = 13.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Set Security PIN", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Text("Protect your schedules and rules", fontSize = 12.sp, color = Zinc500)
                            }
                            Button(
                                onClick = { showSetPinDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                            ) {
                                Text("Set PIN", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Appearance
        item {
            SettingsSectionHeader(title = "APPEARANCE")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    ThemeOptionRow(label = "Clean Dark (Minimal)", isSelected = userPreferences.themeMode == ThemeMode.DARK) {
                        onSetThemeMode(ThemeMode.DARK)
                    }
                    ThemeOptionRow(label = "Refined Clean (Light)", isSelected = userPreferences.themeMode == ThemeMode.LIGHT) {
                        onSetThemeMode(ThemeMode.LIGHT)
                    }
                    ThemeOptionRow(label = "Match System Theme", isSelected = userPreferences.themeMode == ThemeMode.SYSTEM) {
                        onSetThemeMode(ThemeMode.SYSTEM)
                    }
                }
            }
        }

        // Section: Permissions & System Integration
        item {
            SettingsSectionHeader(title = "PERMISSIONS & INTEGRATION")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PermissionStatusRow(
                        title = "Usage Access",
                        subtitle = "Required to calculate app usage & enforce limits",
                        isGranted = hasUsageAccess,
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        }
                    )

                    PermissionStatusRow(
                        title = "Accessibility Protection",
                        subtitle = "Required to detect and lock blocked applications",
                        isGranted = true,
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                    )

                    PermissionStatusRow(
                        title = "Notifications",
                        subtitle = "Protection status and upcoming lock reminders",
                        isGranted = userPreferences.notificationsEnabled,
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )

                    PermissionStatusRow(
                        title = "Battery Optimization",
                        subtitle = "Ensures background schedules are never killed",
                        isGranted = isBatteryOptimized,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }

        // Section: Tamper & Uninstall Protection Info
        item {
            SettingsSectionHeader(title = "TAMPER & UNINSTALL PROTECTION")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp))
                    .clickable { showTamperInfoDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VioletAccent.copy(alpha = 0.12f))
                            .border(1.dp, Color(0x228B5CF6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Personal Device Mode", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("Learn about tamper protection & Device Owner mode", fontSize = 12.sp, color = Zinc500)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Zinc500, modifier = Modifier.size(13.dp))
                }
            }
        }

        // Section: Data & Reset
        item {
            SettingsSectionHeader(title = "DATA MANAGEMENT")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showResetConfirmation = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Reset All Data & Rules", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFFF43F5E))
                            Text("Deletes all schedules, blocked app selections, and statistics", fontSize = 12.sp, color = Zinc500)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Set PIN Dialog
    if (showSetPinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var step by remember { mutableIntStateOf(1) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = { Text(if (step == 1) "Enter New 4-Digit PIN" else "Confirm PIN", color = Color.White, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (step == 1) "Enter a 4-digit security PIN." else "Re-enter your PIN to verify.",
                        color = Zinc400,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val activeDigits = if (step == 1) newPin else confirmPin
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        for (i in 0..3) {
                            val filled = i < activeDigits.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (filled) CyanAccent else Color(0xFF27272A))
                                    .border(1.dp, if (filled) CyanAccent else MinimalBorder, CircleShape)
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(errorMsg ?: "", color = Color(0xFFF43F5E), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Keypad
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "DEL")
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in keys) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                for (k in row) {
                                    com.example.presentation.components.KeypadButton(
                                        label = k,
                                        enabled = true,
                                        onClick = {
                                            when (k) {
                                                "C" -> {
                                                    if (step == 1) newPin = "" else confirmPin = ""
                                                    errorMsg = null
                                                }
                                                "DEL" -> {
                                                    if (step == 1) {
                                                        if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                                                    } else {
                                                        if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                                    }
                                                    errorMsg = null
                                                }
                                                else -> {
                                                    if (step == 1) {
                                                        if (newPin.length < 4) {
                                                            newPin += k
                                                            if (newPin.length == 4) {
                                                                step = 2
                                                            }
                                                        }
                                                    } else {
                                                        if (confirmPin.length < 4) {
                                                            confirmPin += k
                                                            if (confirmPin.length == 4) {
                                                                if (newPin == confirmPin) {
                                                                    onSetPin(newPin)
                                                                    showSetPinDialog = false
                                                                } else {
                                                                    errorMsg = "PINs do not match. Try again."
                                                                    confirmPin = ""
                                                                    step = 1
                                                                    newPin = ""
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false }) { Text("Cancel", color = Zinc400) }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Emergency Unlock Duration Selection Dialog
    if (showEmergencyDurationDialog) {
        val durations = listOf(5, 10, 15, 30)
        AlertDialog(
            onDismissRequest = { showEmergencyDurationDialog = false },
            title = { Text("Emergency Unlock Duration", color = Color.White, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    durations.forEach { minutes ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetEmergencyUnlockDuration(minutes)
                                    showEmergencyDurationDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = userPreferences.emergencyUnlockDurationMinutes == minutes,
                                onClick = {
                                    onSetEmergencyUnlockDuration(minutes)
                                    showEmergencyDurationDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanAccent, unselectedColor = Zinc500)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$minutes minutes", color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showEmergencyDurationDialog = false }) { Text("Cancel", color = Zinc400) }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Tamper & Device Owner info dialog
    if (showTamperInfoDialog) {
        AlertDialog(
            onDismissRequest = { showTamperInfoDialog = false },
            title = { Text("Tamper & Uninstall Behavior", color = Color.White, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "• Standard Personal Device Mode:",
                        fontWeight = FontWeight.SemiBold,
                        color = CyanAccent,
                        fontSize = 14.sp
                    )
                    Text(
                        "Under normal Android permissions, removing/uninstalling FocusLock will disable all blocking rules. Background execution rules and safety standards are fully respected without hidden root exploits or deceptive malware tactics.",
                        fontSize = 13.sp,
                        color = Zinc400
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "• Managed Device / Device Owner Mode:",
                        fontWeight = FontWeight.SemiBold,
                        color = VioletAccent,
                        fontSize = 14.sp
                    )
                    Text(
                        "If provisioned via Android Enterprise / Device Owner mode, the device administrator policy can strictly prevent uninstallation and enforce kiosk / lockout policies via official Android DevicePolicyManager APIs.",
                        fontSize = 13.sp,
                        color = Zinc400
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTamperInfoDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Understood", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset All Data?", color = Color(0xFFF43F5E), fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "This will clear all blocked apps, schedules, usage statistics, and PIN security. This action cannot be undone.",
                    color = Zinc400,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmation = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E), contentColor = Color.White)
                ) {
                    Text("Reset All Data", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("Cancel", color = Zinc400) }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = Zinc500,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (enabled) Color.White else Zinc500)
            Text(subtitle, fontSize = 12.sp, color = Zinc500)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = Zinc400,
                uncheckedTrackColor = Color(0xFF27272A)
            )
        )
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 14.sp, color = CyanAccent, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Zinc500, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
fun ThemeOptionRow(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = if (isSelected) CyanAccent else Color.White, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent, unselectedColor = Zinc500)
        )
    }
}

@Composable
fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, fontSize = 12.sp, color = Zinc500)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGranted) EmeraldSuccess.copy(alpha = 0.12f) else Color(0xFFF43F5E).copy(alpha = 0.12f))
                .border(1.dp, if (isGranted) Color(0x2210B981) else Color(0x22F43F5E), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGranted) "Granted ✓" else "Not Granted →",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isGranted) EmeraldSuccess else Color(0xFFF43F5E)
            )
        }
    }
}
