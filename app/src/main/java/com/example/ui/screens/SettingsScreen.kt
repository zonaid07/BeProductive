package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.EnglishContentBank
import com.example.ui.components.InfoTipCard
import com.example.viewmodel.DailyPathViewModel

@Composable
fun SettingsScreen(
    viewModel: DailyPathViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var nameDraft by remember(profile.userName) { mutableStateOf(profile.userName) }
    var cacheCleared by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Profile & Settings", style = MaterialTheme.typography.titleLarge)
            }
        }

        // User Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Personal Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_name_input")
                    )
                    Button(
                        onClick = {
                            if (nameDraft.isNotBlank()) {
                                viewModel.updateProfile { it.copy(userName = nameDraft.trim()) }
                            }
                        },
                        modifier = Modifier.testTag("settings_save_name_button")
                    ) { Text("Save Name") }
                }
            }
        }

        // Language & Appearance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Language & Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = profile.preferredLanguage == "EN",
                            onClick = { viewModel.updateProfile { it.copy(preferredLanguage = "EN") } },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = profile.preferredLanguage == "BN",
                            onClick = { viewModel.updateProfile { it.copy(preferredLanguage = "BN") } },
                            label = { Text("বাংলা (Bangla)") }
                        )
                    }
                    Text("Appearance Mode", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                            FilterChip(
                                selected = profile.themeMode == mode,
                                onClick = { viewModel.updateProfile { it.copy(themeMode = mode) } },
                                label = { Text(mode) },
                                modifier = Modifier.testTag("theme_mode_$mode")
                            )
                        }
                    }
                }
            }
        }

        // Learning & Reminders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("English Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EnglishContentBank.levels.take(2).forEach { lvl ->
                            FilterChip(
                                selected = profile.englishLevel == lvl,
                                onClick = { viewModel.updateProfile { it.copy(englishLevel = lvl) } },
                                label = { Text(lvl) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EnglishContentBank.levels.drop(2).forEach { lvl ->
                            FilterChip(
                                selected = profile.englishLevel == lvl,
                                onClick = { viewModel.updateProfile { it.copy(englishLevel = lvl) } },
                                label = { Text(lvl) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Daily Habit Reminder", style = MaterialTheme.typography.titleSmall)
                            Text("Morning check-in at ${profile.dailyReminderTime}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = profile.dailyReminderEnabled,
                            onCheckedChange = { enabled -> viewModel.updateProfile { it.copy(dailyReminderEnabled = enabled) } }
                        )
                    }
                }
            }
        }

        // Storage, Cache & Cloud Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Offline Storage & Cache", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "All tasks, prayer alarms, vocabulary progress, reading bookmarks, and history quiz scores are stored locally in Room SQLite.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.clearNewsCache()
                            cacheCleared = true
                        },
                        modifier = Modifier.fillMaxWidth().testTag("clear_cache_button")
                    ) {
                        Text(if (cacheCleared) "Non-Bookmarked News Cache Cleared ✓" else "Clear Cached News")
                    }
                }
            }
        }

        item {
            InfoTipCard(
                title = "Live Gemini AI & Local Storage Status",
                body = "Personal AI Teacher, English translation evaluation, and news summarization are connected to live Gemini AI via Firebase AI Logic (App Check). All tasks, prayer alarms, PDFs, reading progress, and vocabulary are stored locally in Room."
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
