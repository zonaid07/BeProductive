package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.DailyPathViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TasksStatsScreen(
    viewModel: DailyPathViewModel,
    onBack: () -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedTaskDate.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Personal") }

    val recentDates = remember {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        (0..6).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }.time.let { fmt.format(it) }
        }
    }

    val tasksForSelected = remember(allTasks, selectedDate) {
        allTasks.filter { it.dateKey == selectedDate }
    }
    val completedSelected = tasksForSelected.count { it.isCompleted }
    val totalSelected = tasksForSelected.size.coerceAtLeast(1)
    val pctSelected = (completedSelected * 100) / totalSelected

    val groupedByDate = remember(allTasks) { allTasks.groupBy { it.dateKey } }
    val activeStreak = remember(groupedByDate) {
        var streak = 0
        for (date in recentDates) {
            val dayTasks = groupedByDate[date].orEmpty()
            if (dayTasks.isNotEmpty() && dayTasks.count { it.isCompleted } >= (dayTasks.size / 2).coerceAtLeast(1)) {
                streak++
            } else {
                break
            }
        }
        streak
    }
    val missedToday = tasksForSelected.filter { !it.isCompleted }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_custom_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add custom task")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text("Daily Task Manager & Stats", style = MaterialTheme.typography.titleLarge)
                        Text("Track habits, streaks, and previous days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Date selector for reviewing previous days
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentDates) { dateKey ->
                        FilterChip(
                            selected = selectedDate == dateKey,
                            onClick = { viewModel.selectTaskDate(dateKey) },
                            label = { Text(if (dateKey == viewModel.todayKey) "Today" else dateKey.substring(5)) }
                        )
                    }
                }
            }

            // Stats Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Completed: $completedSelected / $totalSelected ($pctSelected%)", fontWeight = FontWeight.Bold)
                            Text("Streak: $activeStreak days", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { completedSelected.toFloat() / totalSelected.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = "Missed / Remaining: ${missedToday.size} • Total tracked days: ${groupedByDate.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            items(tasksForSelected, key = { it.compositeId }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTask(task.compositeId, !task.isCompleted) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle task",
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.titleEn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("${task.category}${if (task.isCustom) " • Custom" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (task.isCustom) {
                            IconButton(onClick = { viewModel.deleteTask(task.compositeId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete custom task")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Daily Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Task title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Category (e.g., Study, Health)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTitle.isNotBlank()) {
                        viewModel.addCustomTask(selectedDate, newTitle, newCategory.ifBlank { "Personal" }, "")
                        newTitle = ""
                        showAddDialog = false
                    }
                }) { Text("Add Task") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
