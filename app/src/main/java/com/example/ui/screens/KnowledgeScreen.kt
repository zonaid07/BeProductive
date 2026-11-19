package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.IslamicHistoryContentBank
import com.example.data.content.IslamicHistoryLesson
import com.example.ui.components.PdfReaderScreen
import com.example.ui.components.SectionHeader
import com.example.ui.theme.NotoSansBengaliFamily
import com.example.viewmodel.DailyPathViewModel

@Composable
fun KnowledgeScreen(viewModel: DailyPathViewModel) {
    val progressMap by viewModel.historyProgress.collectAsStateWithLifecycle()
    val allPdfs by viewModel.userPdfs.collectAsStateWithLifecycle()
    val historyPdfs = remember(allPdfs) { allPdfs.filter { it.section == "HISTORY" } }

    var selectedCategory by remember { mutableStateOf("All") }
    var activeLesson by remember { mutableStateOf<IslamicHistoryLesson?>(null) }
    var openPdfId by remember { mutableStateOf<String?>(null) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importUserPdf(uri, "HISTORY") { msg -> uploadStatus = msg }
        }
    }

    val activePdf = historyPdfs.firstOrNull { it.id == openPdfId }
    if (activePdf != null) {
        PdfReaderScreen(
            pdf = activePdf,
            todayDateKey = viewModel.todayKey,
            onBack = { openPdfId = null },
            onPageChanged = { newPage -> viewModel.updatePdfPage(activePdf.id, newPage, false) },
            onToggleBookmark = { page -> viewModel.updatePdfPage(activePdf.id, page, true) }
        )
        return
    }

    if (activeLesson != null) {
        val lesson = activeLesson!!
        BackHandler { activeLesson = null }
        HistoryLessonDetail(
            lesson = lesson,
            onBack = { activeLesson = null },
            onSubmitQuiz = { score, total ->
                viewModel.saveHistoryQuizResult(lesson.id, score, total)
            }
        )
        return
    }

    val filtered = remember(selectedCategory) {
        if (selectedCategory == "All") IslamicHistoryContentBank.lessons
        else IslamicHistoryContentBank.lessons.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Islamic History & Personal PDFs", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Structured daily lessons in simple Bangla plus your personal Islamic history PDF books",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Upload Personal History PDF Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Personal History & Seerah PDF Library",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upload any PDF book and read as many pages as you want each day — your page automatically pauses and resumes next day.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.fillMaxWidth().testTag("upload_history_pdf_button")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upload Personal History PDF")
                    }
                    if (uploadStatus != null) {
                        Text(uploadStatus!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (historyPdfs.isNotEmpty()) {
            item {
                SectionHeader(title = "My Uploaded History PDFs")
            }
            items(historyPdfs, key = { it.id }) { pdf ->
                val pdfToday = if (pdf.lastReadDateKey == viewModel.todayKey) pdf.pagesReadToday else 0
                Card(
                    onClick = { openPdfId = pdf.id },
                    modifier = Modifier.fillMaxWidth().testTag("history_pdf_${pdf.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pdf.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Paused at Page ${pdf.currentPage} of ${pdf.totalPages} • Today: $pdfToday pages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.deleteUserPdf(pdf.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete PDF")
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Structured Daily Lessons")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(IslamicHistoryContentBank.categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { lesson ->
            val prog = progressMap[lesson.id]
            Card(
                onClick = { activeLesson = lesson },
                modifier = Modifier.fillMaxWidth().testTag("history_lesson_${lesson.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${lesson.category} • ${lesson.eraLabel}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        if (prog?.completed == true) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        text = lesson.titleBn,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = NotoSansBengaliFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(lesson.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text(lesson.sourceTypeBadge, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (prog != null) {
                        Text(
                            text = "Last Quiz Score: ${prog.quizScore}/${prog.totalQuestions}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryLessonDetail(
    lesson: IslamicHistoryLesson,
    onBack: () -> Unit,
    onSubmitQuiz: (Int, Int) -> Unit
) {
    val selectedAnswers = remember(lesson.id) { mutableStateMapOf<Int, Int>() }
    val shortAnswers = remember(lesson.id) { mutableStateMapOf<Int, String>() }
    var submitted by remember(lesson.id) { mutableStateOf(false) }

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to lessons")
                }
                Column {
                    Text(lesson.titleBn, style = MaterialTheme.typography.titleLarge, fontFamily = NotoSansBengaliFamily)
                    Text("${lesson.eraLabel} • ${lesson.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Source Verification: ${lesson.sourceTypeBadge}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    lesson.references.forEach { ref ->
                        Text("• $ref", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ঐতিহাসিক বিবরণ (Lesson Overview)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(lesson.overviewBn, style = MaterialTheme.typography.bodyLarge, fontFamily = NotoSansBengaliFamily)
                    Spacer(Modifier.height(4.dp))
                    Text("গুরুত্বপূর্ণ ব্যক্তিত্ব:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(lesson.keyPersonalities.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("কালানুক্রম (Historical Timeline)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    lesson.timeline.forEach { ev ->
                        Column {
                            Text(ev.dateLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(ev.eventBn, style = MaterialTheme.typography.bodyMedium, fontFamily = NotoSansBengaliFamily)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ঐতিহাসিক তাৎপর্য ও সারসংক্ষেপ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(lesson.significanceBn, style = MaterialTheme.typography.bodyMedium, fontFamily = NotoSansBengaliFamily)
                    Spacer(Modifier.height(4.dp))
                    Text("Summary: ${lesson.summaryBn}", style = MaterialTheme.typography.bodySmall, fontFamily = NotoSansBengaliFamily)
                }
            }
        }

        item {
            Text("Knowledge Assessment (5 MCQs)", style = MaterialTheme.typography.titleLarge)
        }

        items(lesson.mcqs.indices.toList()) { idx ->
            val q = lesson.mcqs[idx]
            val chosen = selectedAnswers[idx]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${idx + 1}. ${q.questionBn}", style = MaterialTheme.typography.titleSmall, fontFamily = NotoSansBengaliFamily)
                    q.options.forEachIndexed { optIdx, opt ->
                        val bg = when {
                            !submitted && chosen == optIdx -> MaterialTheme.colorScheme.primaryContainer
                            submitted && optIdx == q.correctIndex -> MaterialTheme.colorScheme.primaryContainer
                            submitted && chosen == optIdx && optIdx != q.correctIndex -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Surface(
                            onClick = { if (!submitted) selectedAnswers[idx] = optIdx },
                            shape = RoundedCornerShape(10.dp),
                            color = bg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(opt, modifier = Modifier.padding(10.dp), fontFamily = NotoSansBengaliFamily)
                        }
                    }
                    if (submitted) {
                        Text(q.explanationBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontFamily = NotoSansBengaliFamily)
                    }
                }
            }
        }

        item {
            Text("Short-Answer Reflection (2 Questions)", style = MaterialTheme.typography.titleLarge)
        }
        items(lesson.shortAnswerPrompts.indices.toList()) { idx ->
            val prompt = lesson.shortAnswerPrompts[idx]
            OutlinedTextField(
                value = shortAnswers[idx].orEmpty(),
                onValueChange = { shortAnswers[idx] = it },
                label = { Text("${idx + 1}. $prompt") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            val score = lesson.mcqs.indices.count { selectedAnswers[it] == lesson.mcqs[it].correctIndex }
            Button(
                onClick = {
                    submitted = true
                    onSubmitQuiz(score, lesson.mcqs.size)
                },
                modifier = Modifier.fillMaxWidth().testTag("submit_history_quiz_button")
            ) {
                Text(if (submitted) "Submitted! Score: $score / ${lesson.mcqs.size}" else "Submit Assessment & View Explanations")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
