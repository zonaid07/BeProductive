package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.content.IslamicHistoryContentBank
import com.example.data.content.LiteratureContentBank
import com.example.data.content.VocabularyContentBank
import com.example.ui.components.MetricPillCard
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.components.SectionHeader
import com.example.ui.theme.NotoSansBengaliFamily
import com.example.viewmodel.DailyPathViewModel

@Composable
fun HomeScreen(
    viewModel: DailyPathViewModel,
    onNavigateToTab: (String) -> Unit,
    onOpenNews: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val tasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val prayerSchedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
    val vocabMap by viewModel.vocabProgress.collectAsStateWithLifecycle()
    val readingMap by viewModel.readingProgress.collectAsStateWithLifecycle()
    val userPdfs by viewModel.userPdfs.collectAsStateWithLifecycle()
    val historyMap by viewModel.historyProgress.collectAsStateWithLifecycle()
    val newsList by viewModel.newsArticles.collectAsStateWithLifecycle()
    val newsLoading by viewModel.newsLoading.collectAsStateWithLifecycle()
    val englishSessions by viewModel.englishSessions.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val completedCount = remember(tasks) { tasks.count { it.isCompleted } }
    val totalCount = tasks.size.coerceAtLeast(1)
    val completionPct = (completedCount * 100) / totalCount

    val dailyVocabWords = remember { VocabularyContentBank.dailyTenWords() }
    val learnedTodayVocab = remember(vocabMap, dailyVocabWords) {
        dailyVocabWords.count { vocabMap[it.id]?.learned == true }
    }
    val pagesReadToday = remember(readingMap, userPdfs, viewModel.todayKey) {
        readingMap.values.filter { it.lastReadDateKey == viewModel.todayKey }.sumOf { it.pagesReadToday } +
            userPdfs.filter { it.lastReadDateKey == viewModel.todayKey }.sumOf { it.pagesReadToday }
    }
    val todayEnglishDone = remember(englishSessions, viewModel.todayKey) {
        englishSessions.any { it.dateKey == viewModel.todayKey && it.completed }
    }
    val todayHistoryLesson = remember(historyMap) {
        IslamicHistoryContentBank.lessons.firstOrNull { historyMap[it.id]?.completed != true }
            ?: IslamicHistoryContentBank.lessons.first()
    }
    val historyCompletedCount = remember(historyMap) {
        historyMap.values.count { it.completed }
    }
    val homeNewsItems = remember(newsList) {
        newsList.filter { it.region == "BANGLADESH" }.take(7) +
            newsList.filter { it.region == "INTERNATIONAL" }.take(3)
    }
    val greeting = remember(profile.preferredLanguage) { viewModel.greetingText() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with Greeting, Date, Location & Quick Actions
        item(key = "home_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, ${profile.userName}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = viewModel.formattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Current location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = profile.cityName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onOpenAiAssistant,
                        modifier = Modifier.testTag("home_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Learning Assistant",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        if (!isOnline) {
            item { OfflineStatusBanner(isOnline = false) }
        }

        // Hero Sanctuary Prayer Card
        item(key = "home_hero_prayer") {
            Card(
                onClick = { onNavigateToTab("prayer") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_prayer_hero_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = R.drawable.img_hero_sanctuary_1795091367054,
                        contentDescription = "DailyPath sanctuary banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xBB072D1F),
                                        Color(0xDD0A3828)
                                    )
                                )
                            )
                    )
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
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "NEXT PRAYER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF6EBD4),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "in ${prayerSchedule.countdownText}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFE3BE73),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "${prayerSchedule.nextPrayer.name} (${prayerSchedule.nextPrayer.nameBn}) • ${prayerSchedule.nextPrayer.time12}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(14.dp))
                        // Compact 5-prayer timetable strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            prayerSchedule.slots.filter { it.isObligatory }.forEach { slot ->
                                val isNext = slot.name == prayerSchedule.nextPrayer.name
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isNext) Color(0x38E3BE73) else Color.Transparent)
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = slot.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isNext) Color(0xFFF6EBD4) else Color(0xCCFFFFFF)
                                    )
                                    Text(
                                        text = slot.time24,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Progress & Interactive Goals Checklist
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_daily_goals_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Progress: $completedCount/$totalCount Completed – $completionPct%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Check off each habit as you complete it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onOpenTasks) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Open full task manager",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / totalCount.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    tasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleTask(task.compositeId, !task.isCompleted) }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .testTag("task_row_${task.taskKey}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (task.isCompleted) "Completed" else "Not completed",
                                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (profile.preferredLanguage == "BN") task.titleBn else task.titleEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = task.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Module Progress Grid (English, Vocabulary, Reading, History)
        item {
            SectionHeader(title = "Today's Growth Path")
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricPillCard(
                    icon = Icons.Default.Translate,
                    label = "English (${profile.englishLevel})",
                    value = if (todayEnglishDone) "Completed ✓" else "10-Min Ready",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab("english") }
                )
                MetricPillCard(
                    icon = Icons.Default.School,
                    label = "Vocabulary",
                    value = "$learnedTodayVocab / 10 Words",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab("vocabulary") }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricPillCard(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "Reading & PDFs",
                    value = "$pagesReadToday Pages Today",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab("reading") }
                )
                MetricPillCard(
                    icon = Icons.Default.HistoryEdu,
                    label = "Islamic History",
                    value = "$historyCompletedCount/${IslamicHistoryContentBank.lessons.size} Done",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab("knowledge") }
                )
            }
        }

        // Today's Islamic History Spotlight
        item {
            SectionHeader(
                title = "Today's Islamic History",
                actionLabel = "Study",
                onActionClick = { onNavigateToTab("knowledge") }
            )
            Card(
                onClick = { onNavigateToTab("knowledge") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${todayHistoryLesson.category} • ${todayHistoryLesson.eraLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = todayHistoryLesson.titleBn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = todayHistoryLesson.summaryBn,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Today's Bengali Literature Spotlight
        item {
            val featuredBook = LiteratureContentBank.books.first()
            SectionHeader(
                title = "Bengali Literature & PDFs",
                actionLabel = "Resume",
                onActionClick = { onNavigateToTab("reading") }
            )
            Card(
                onClick = { onNavigateToTab("reading") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = R.drawable.img_bengali_heritage_1795091367506,
                        contentDescription = "Bengali literature artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = featuredBook.titleBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${featuredBook.authorBn} • ${featuredBook.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Read today: $pagesReadToday pages • Auto-pauses where you stop",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Latest Bangladesh & International News Headlines
        item(key = "home_news") {
            SectionHeader(
                title = "Bangladesh & World News",
                subtitle = "Jamuna TV, Channel 24, Ekattor, Independent, RTV, Daily Star & Daily Campus",
                actionLabel = if (newsLoading) "Updating..." else "All News",
                onActionClick = onOpenNews
            )
            if (homeNewsItems.isEmpty()) {
                Card(
                    onClick = { viewModel.refreshNews(); onOpenNews() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Newspaper, contentDescription = "News", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (newsLoading) "Fetching live Bangladesh & World headlines..." else "Tap to fetch live Bangladesh & World headlines.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(homeNewsItems, key = { it.id }) { article ->
                        Card(
                            onClick = onOpenNews,
                            modifier = Modifier.width(270.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = if (article.region == "BANGLADESH") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (article.region == "BANGLADESH") "BD • ${article.source}" else "World • ${article.source}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (article.region == "BANGLADESH") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = article.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = NotoSansBengaliFamily,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = article.publishedAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
