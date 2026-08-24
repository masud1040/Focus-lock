package com.example.presentation.schedules

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ScheduleEntity
import com.example.domain.model.InstalledApp
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalBorderSubtle
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import java.util.Locale

@Composable
fun SchedulesScreen(
    schedules: List<ScheduleEntity>,
    installedApps: List<InstalledApp>,
    onSaveSchedule: (ScheduleEntity) -> Unit,
    onDeleteSchedule: (Long) -> Unit,
    onToggleSchedule: (Long, Boolean) -> Unit
) {
    var editingSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = CyanAccent,
                contentColor = Color(0xFF050505),
                shape = CircleShape,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Schedules",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Recurring blocking rules & night curfews",
                        fontSize = 13.sp,
                        color = Zinc500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF18181B))
                                .border(1.dp, MinimalBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No schedules yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + to create your first blocking schedule (e.g. YouTube 22:00 → 07:00).",
                            fontSize = 13.sp,
                            color = Zinc400,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleItemCard(
                            schedule = schedule,
                            installedApps = installedApps,
                            onToggle = { isEnabled -> onToggleSchedule(schedule.id, isEnabled) },
                            onEdit = { editingSchedule = schedule },
                            onDelete = { onDeleteSchedule(schedule.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (isAddingNew || editingSchedule != null) {
        ScheduleEditDialog(
            initialSchedule = editingSchedule,
            installedApps = installedApps,
            onDismiss = {
                isAddingNew = false
                editingSchedule = null
            },
            onSave = { savedSchedule ->
                onSaveSchedule(savedSchedule)
                isAddingNew = false
                editingSchedule = null
            }
        )
    }
}

@Composable
fun ScheduleItemCard(
    schedule: ScheduleEntity,
    installedApps: List<InstalledApp>,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val targetAppName = remember(schedule.packageName, installedApps) {
        if (schedule.packageName.isBlank()) "All Blocked Apps"
        else installedApps.firstOrNull { it.packageName == schedule.packageName }?.appName ?: schedule.packageName
    }

    val isOvernight = schedule.startHour > schedule.endHour ||
            (schedule.startHour == schedule.endHour && schedule.startMinute > schedule.endMinute)

    val timeFormatted = String.format(
        Locale.getDefault(),
        "%02d:%02d → %02d:%02d",
        schedule.startHour,
        schedule.startMinute,
        schedule.endHour,
        schedule.endMinute
    )

    val activeDays = remember(schedule.daysOfWeek) {
        schedule.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (schedule.isEnabled) Color(0x3322D3EE) else MinimalBorderSubtle,
                RoundedCornerShape(20.dp)
            )
            .testTag("schedule_item_${schedule.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Target: $targetAppName",
                        fontSize = 12.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                }

                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("schedule_switch_${schedule.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF050505),
                        checkedTrackColor = CyanAccent,
                        uncheckedThumbColor = Zinc400,
                        uncheckedTrackColor = Color(0xFF27272A)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Overnight Badge Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF27272A).copy(alpha = 0.5f))
                        .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                if (isOvernight) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VioletAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nightlight,
                            contentDescription = "Overnight",
                            tint = VioletAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Overnight (+1d)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = VioletAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days of week indicator pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (i in 1..7) {
                        val isSelected = activeDays.contains(i)
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) CyanAccent else Color(0xFF27272A).copy(alpha = 0.5f))
                                .border(1.dp, if (isSelected) CyanAccent else MinimalBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNames[i - 1],
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF050505) else Zinc400
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Schedule",
                            tint = Zinc400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Schedule",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditDialog(
    initialSchedule: ScheduleEntity?,
    installedApps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onSave: (ScheduleEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialSchedule?.title ?: "Focus Schedule") }
    var selectedPackage by remember { mutableStateOf(initialSchedule?.packageName ?: "") }

    var startHour by remember { mutableIntStateOf(initialSchedule?.startHour ?: 22) }
    var startMinute by remember { mutableIntStateOf(initialSchedule?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(initialSchedule?.endHour ?: 7) }
    var endMinute by remember { mutableIntStateOf(initialSchedule?.endMinute ?: 0) }

    var selectedDays by remember {
        val days = initialSchedule?.daysOfWeek ?: "1,2,3,4,5,6,7"
        mutableStateOf(days.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet())
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var isAppDropdownExpanded by remember { mutableStateOf(false) }

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSchedule == null) "Create New Schedule" else "Edit Schedule",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Schedule Name", color = Zinc400) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF18181B).copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF18181B).copy(alpha = 0.5f),
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = MinimalBorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target App Selector
                ExposedDropdownMenuBox(
                    expanded = isAppDropdownExpanded,
                    onExpandedChange = { isAppDropdownExpanded = it }
                ) {
                    val appLabel = if (selectedPackage.isBlank()) "All Blocked Apps (Global)"
                    else installedApps.firstOrNull { it.packageName == selectedPackage }?.appName ?: selectedPackage

                    OutlinedTextField(
                        value = appLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Application", color = Zinc400) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAppDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF18181B).copy(alpha = 0.5f),
                            unfocusedContainerColor = Color(0xFF18181B).copy(alpha = 0.5f),
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = MinimalBorderSubtle
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isAppDropdownExpanded,
                        onDismissRequest = { isAppDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Blocked Apps (Global)") },
                            onClick = {
                                selectedPackage = ""
                                isAppDropdownExpanded = false
                            }
                        )
                        installedApps.forEach { app ->
                            DropdownMenuItem(
                                text = { Text(app.appName) },
                                onClick = {
                                    selectedPackage = app.packageName
                                    isAppDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Time
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { showStartPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("START TIME", fontSize = 10.sp, color = Zinc500, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanAccent
                            )
                        }
                    }

                    // End Time
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { showEndPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("END TIME", fontSize = 10.sp, color = Zinc500, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BlueAccent
                            )
                        }
                    }
                }

                val isOvernight = startHour > endHour || (startHour == endHour && startMinute > endMinute)
                if (isOvernight) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🌙 Overnight schedule: Ends the next morning",
                        fontSize = 11.sp,
                        color = VioletAccent,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days Selection
                Text("REPEAT DAYS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Zinc400)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 1..7) {
                        val isSelected = selectedDays.contains(i)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) CyanAccent else Color(0xFF27272A).copy(alpha = 0.5f))
                                .border(1.dp, if (isSelected) CyanAccent else MinimalBorderSubtle, CircleShape)
                                .clickable {
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - i else selectedDays
                                    } else {
                                        selectedDays + i
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNames[i - 1].take(1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF050505) else Zinc400
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { selectedDays = setOf(1, 2, 3, 4, 5, 6, 7) }) {
                        Text("All Days", fontSize = 11.sp, color = CyanAccent)
                    }
                    TextButton(onClick = { selectedDays = setOf(1, 2, 3, 4, 5) }) {
                        Text("Weekdays", fontSize = 11.sp, color = CyanAccent)
                    }
                    TextButton(onClick = { selectedDays = setOf(6, 7) }) {
                        Text("Weekends", fontSize = 11.sp, color = CyanAccent)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysCsv = selectedDays.sorted().joinToString(",")
                    val newEntity = ScheduleEntity(
                        id = initialSchedule?.id ?: 0L,
                        packageName = selectedPackage,
                        title = title.ifBlank { "Schedule" },
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = daysCsv,
                        isEnabled = initialSchedule?.isEnabled ?: true
                    )
                    onSave(newEntity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF050505)),
                modifier = Modifier.testTag("save_schedule_confirm_button")
            ) {
                Text("Save Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Zinc400)
            }
        },
        containerColor = Color(0xFF18181B),
        shape = RoundedCornerShape(24.dp)
    )

    // Start Time Picker Dialog
    if (showStartPicker) {
        val pickerState = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showStartPicker = false },
            title = { Text("Select Start Time", color = Color.White) },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                Button(
                    onClick = {
                        startHour = pickerState.hour
                        startMinute = pickerState.minute
                        showStartPicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF050505))
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel", color = Zinc400) }
            },
            containerColor = Color(0xFF18181B)
        )
    }

    // End Time Picker Dialog
    if (showEndPicker) {
        val pickerState = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showEndPicker = false },
            title = { Text("Select End Time", color = Color.White) },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                Button(
                    onClick = {
                        endHour = pickerState.hour
                        endMinute = pickerState.minute
                        showEndPicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF050505))
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel", color = Zinc400) }
            },
            containerColor = Color(0xFF18181B)
        )
    }
}
