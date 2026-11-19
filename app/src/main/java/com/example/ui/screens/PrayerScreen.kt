package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PrayerAlarmEntity
import com.example.prayer.AsrMethod
import com.example.prayer.CalculationMethod
import com.example.prayer.PrayerAlarmScheduler
import com.example.prayer.PrayerTimeCalculator
import com.example.ui.components.InfoTipCard
import com.example.ui.components.SectionHeader
import com.example.viewmodel.DailyPathViewModel
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun PrayerScreen(viewModel: DailyPathViewModel) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val schedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
    val alarms by viewModel.prayerAlarms.collectAsStateWithLifecycle()
    val alarmMap = remember(alarms) { alarms.associateBy { it.prayerName } }

    var showCityPicker by remember { mutableStateOf(false) }
    var showMethodPicker by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<PrayerAlarmEntity?>(null) }
    var editingDefaultTime24 by remember { mutableStateOf("05:00") }
    var showTahajjudDialog by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf<String?>(null) }

    val hasNotificationPermission = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }
    var notifGranted by remember { mutableStateOf(hasNotificationPermission) }
    val exactAlarmAllowed = PrayerAlarmScheduler.canScheduleExactAlarms(context)

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notifGranted = granted }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val latStr = "%.2f".format(loc.latitude)
                    val lonStr = "%.2f".format(loc.longitude)
                    viewModel.updateDetectedLocation(loc.latitude, loc.longitude, "GPS ($latStr, $lonStr)")
                    locationMessage = "Location updated via GPS."
                } else {
                    locationMessage = "GPS fix unavailable — please pick a city manually."
                }
            }.addOnFailureListener {
                locationMessage = "Could not fetch GPS location — use manual city selection."
            }
        } else {
            locationMessage = "Location permission denied. You can select any city manually."
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Prayer Times & Custom Alarms",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${profile.cityName} • ${CalculationMethod.fromKey(profile.calculationMethod).displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Next Prayer Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "UPCOMING PRAYER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${schedule.nextPrayer.name} (${schedule.nextPrayer.nameBn})",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = schedule.nextPrayer.time12,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "in ${schedule.countdownText}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Location & Method Controls
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detect_location_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Detect GPS")
                }
                OutlinedButton(
                    onClick = { showCityPicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_city_button")
                ) {
                    Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Select City")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showMethodPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_method_button")
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Method: ${profile.calculationMethod} • Asr: ${profile.asrMethod}")
            }
            if (locationMessage != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = locationMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Permission Warnings
        if (!notifGranted || !exactAlarmAllowed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Alarm & Notification Permissions Needed",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        if (!notifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Grant Notification Permission")
                            }
                        }
                        if (!exactAlarmAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            OutlinedButton(onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                runCatching { context.startActivity(intent) }
                            }) {
                                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Allow Exact Prayer Alarms")
                            }
                        }
                    }
                }
            }
        }

        // Today's Prayer Timetable + Fully Customizable Per-Prayer Alarms
        item {
            SectionHeader(
                title = "Today's Prayer Schedule",
                subtitle = "Tap the settings icon on any prayer to set custom time, ringtone, days & label"
            )
        }

        items(schedule.slots, key = { it.name }) { slot ->
            val alarm = alarmMap[slot.name] ?: PrayerAlarmEntity(prayerName = slot.name)
            val isNext = slot.name == schedule.nextPrayer.name
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${slot.name} (${slot.nameBn})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (slot.isObligatory) {
                            val timeSummary = if (alarm.useCustomTime && alarm.customTime24.isNotBlank()) {
                                "Custom ${alarm.customTime24}"
                            } else {
                                "${alarm.reminderMinutesBefore}m before"
                            }
                            val soundSummary = if (alarm.soundMode == "CUSTOM") alarm.customSoundName else alarm.soundMode
                            Text(
                                text = if (alarm.enabled) {
                                    "$timeSummary • $soundSummary"
                                } else {
                                    "Alarm muted"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (alarm.customLabel.isNotBlank()) {
                                Text(
                                    text = alarm.customLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(
                                text = "End of Fajr time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = slot.time12,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (slot.isObligatory) {
                        Spacer(Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                editingDefaultTime24 = slot.time24
                                editingAlarm = alarm
                            },
                            modifier = Modifier.testTag("customize_alarm_${slot.name}")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Customize ${slot.name} alarm")
                        }
                        Switch(
                            checked = alarm.enabled,
                            onCheckedChange = { enabled ->
                                viewModel.updatePrayerAlarm(alarm.copy(enabled = enabled))
                            }
                        )
                    }
                }
            }
        }

        // Tahajjud Separate Reminder Card with Custom Time
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tahajjud Reminder (তাহাজ্জুদ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Custom time: ${profile.tahajjudTime} (tap settings to change)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showTahajjudDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Set Tahajjud time")
                    }
                    Switch(
                        checked = profile.tahajjudEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateProfile { it.copy(tahajjudEnabled = enabled) }
                        }
                    )
                }
            }
        }

        // Battery Optimization Info
        item {
            InfoTipCard(
                title = "Android Battery Optimization Notice",
                body = "Some Android devices delay background alarms in Doze mode. For reliable custom prayer alarms, exclude DailyPath from battery optimization in system App Settings. Alarms are automatically restored after device reboot."
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    // City Picker Dialog
    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            title = { Text("Select City") },
            text = {
                LazyColumn(modifier = Modifier.height(340.dp)) {
                    items(PrayerTimeCalculator.popularCities) { city ->
                        TextButton(
                            onClick = {
                                viewModel.selectCity(city)
                                showCityPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(city.displayName, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityPicker = false }) { Text("Close") }
            }
        )
    }

    // Calculation & Asr Method Dialog
    if (showMethodPicker) {
        AlertDialog(
            onDismissRequest = { showMethodPicker = false },
            title = { Text("Prayer Calculation Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Calculation Method", style = MaterialTheme.typography.titleSmall)
                    CalculationMethod.entries.forEach { method ->
                        FilterChip(
                            selected = profile.calculationMethod == method.key,
                            onClick = { viewModel.updateCalculationMethod(method.key) },
                            label = { Text(method.displayName, maxLines = 1) }
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Asr Juristic Method", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AsrMethod.entries.forEach { asr ->
                            FilterChip(
                                selected = profile.asrMethod == asr.key,
                                onClick = { viewModel.updateAsrMethod(asr.key) },
                                label = { Text(asr.key) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showMethodPicker = false }) { Text("Done") }
            }
        )
    }

    // Tahajjud Custom Time Dialog
    if (showTahajjudDialog) {
        var tahajjudInput by remember(profile.tahajjudTime) { mutableStateOf(profile.tahajjudTime) }
        AlertDialog(
            onDismissRequest = { showTahajjudDialog = false },
            title = { Text("Set Custom Tahajjud Time") },
            text = {
                OutlinedTextField(
                    value = tahajjudInput,
                    onValueChange = { tahajjudInput = it },
                    label = { Text("Time in 24h format (HH:MM)") },
                    placeholder = { Text("03:45") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val cleaned = normalizeTime24(tahajjudInput, "03:45")
                    viewModel.updateProfile { it.copy(tahajjudTime = cleaned, tahajjudEnabled = true) }
                    showTahajjudDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showTahajjudDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Fully Customizable Per-Prayer Alarm Dialog
    editingAlarm?.let { current ->
        var useCustomTime by remember(current) { mutableStateOf(current.useCustomTime) }
        var customTimeText by remember(current) {
            mutableStateOf(current.customTime24.ifBlank { editingDefaultTime24 })
        }
        var soundMode by remember(current) { mutableStateOf(current.soundMode) }
        var customSoundUri by remember(current) { mutableStateOf(current.customSoundUri) }
        var customSoundName by remember(current) { mutableStateOf(current.customSoundName) }
        var customLabel by remember(current) { mutableStateOf(current.customLabel) }
        var reminderMinInput by remember(current) { mutableStateOf(current.reminderMinutesBefore.toString()) }
        var vibrate by remember(current) { mutableStateOf(current.vibrationEnabled) }
        var volume by remember(current) { mutableStateOf(current.volumePercent.toFloat()) }
        var repeatDays by remember(current) {
            mutableStateOf(
                current.repeatDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet().ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }
            )
        }

        val ringtoneLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    res.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    res.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
                if (uri != null) {
                    customSoundUri = uri.toString()
                    customSoundName = RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: "Custom Tone"
                    soundMode = "CUSTOM"
                }
            }
        }

        val dayLabels = listOf(1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat")

        AlertDialog(
            onDismissRequest = { editingAlarm = null },
            title = { Text("Customize ${current.prayerName} Alarm") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Custom Time Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use My Custom Time", style = MaterialTheme.typography.labelLarge)
                            Text("Override calculated prayer time", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = useCustomTime, onCheckedChange = { useCustomTime = it })
                    }

                    if (useCustomTime) {
                        OutlinedTextField(
                            value = customTimeText,
                            onValueChange = { customTimeText = it },
                            label = { Text("Custom Alarm Time (24h HH:MM)") },
                            placeholder = { Text(editingDefaultTime24) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Remind Before Prayer (minutes)", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0, 5, 10, 15, 30).forEach { min ->
                                FilterChip(
                                    selected = reminderMinInput == min.toString(),
                                    onClick = { reminderMinInput = min.toString() },
                                    label = { Text("${min}m") }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = reminderMinInput,
                            onValueChange = { reminderMinInput = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Or enter custom minutes before") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Custom Label
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("Custom Alarm Note / Label (optional)") },
                        placeholder = { Text("e.g., Fajr Jamat at 5:30") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Sound Mode + Device Ringtone Picker
                    Text("Alarm Sound", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ADHAN", "NOTIFICATION", "CUSTOM").forEach { mode ->
                            FilterChip(
                                selected = soundMode == mode,
                                onClick = { soundMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose ${current.prayerName} Alarm Sound")
                                if (customSoundUri.isNotBlank()) {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(customSoundUri))
                                }
                            }
                            runCatching { ringtoneLauncher.launch(intent) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick Device Sound: $customSoundName")
                    }

                    // Repeat Days
                    Text("Repeat Days", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dayLabels.forEach { (dayNum, label) ->
                            val selected = dayNum in repeatDays
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    repeatDays = if (selected && repeatDays.size > 1) repeatDays - dayNum else repeatDays + dayNum
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibration")
                        Switch(checked = vibrate, onCheckedChange = { vibrate = it })
                    }

                    Text("Alarm Volume: ${volume.roundToInt()}%")
                    Slider(value = volume, onValueChange = { volume = it }, valueRange = 10f..100f)

                    OutlinedButton(
                        onClick = {
                            val previewConfig = current.copy(
                                soundMode = soundMode,
                                customSoundUri = customSoundUri,
                                customSoundName = customSoundName,
                                customLabel = customLabel.trim(),
                                vibrationEnabled = vibrate,
                                reminderMinutesBefore = reminderMinInput.toIntOrNull() ?: 0
                            )
                            viewModel.testPrayerAlarm(previewConfig)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Test Alarm Notification Now")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val normalizedCustomTime = normalizeTime24(customTimeText, editingDefaultTime24)
                    val safeMinutes = (reminderMinInput.toIntOrNull() ?: 0).coerceIn(0, 180)
                    viewModel.updatePrayerAlarm(
                        current.copy(
                            enabled = true,
                            useCustomTime = useCustomTime,
                            customTime24 = if (useCustomTime) normalizedCustomTime else "",
                            soundMode = soundMode,
                            customSoundUri = customSoundUri,
                            customSoundName = customSoundName,
                            customLabel = customLabel.trim(),
                            repeatDaysCsv = repeatDays.sorted().joinToString(","),
                            reminderMinutesBefore = if (useCustomTime) 0 else safeMinutes,
                            vibrationEnabled = vibrate,
                            volumePercent = volume.roundToInt()
                        )
                    )
                    editingAlarm = null
                }) { Text("Save Alarm") }
            },
            dismissButton = {
                TextButton(onClick = { editingAlarm = null }) { Text("Cancel") }
            }
        )
    }
}

private fun normalizeTime24(raw: String, fallback: String): String {
    val parts = raw.trim().split(":")
    val h = parts.getOrNull(0)?.toIntOrNull()
    val m = parts.getOrNull(1)?.toIntOrNull()
    return if (h != null && m != null && h in 0..23 && m in 0..59) {
        "%02d:%02d".format(h, m)
    } else {
        fallback
    }
}
