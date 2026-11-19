package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TranslationEvaluation
import com.example.data.content.EnglishContentBank
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.components.SectionHeader
import com.example.viewmodel.DailyPathViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnglishScreen(
    viewModel: DailyPathViewModel,
    onOpenAiAssistant: () -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val sessions by viewModel.englishSessions.collectAsStateWithLifecycle()
    val lesson = remember(profile.englishLevel) {
        EnglishContentBank.getLessonForLevel(profile.englishLevel)
    }

    val sections = listOf("A. EN → BN", "B. BN → EN", "C. Grammar", "D. Sentence", "E. Challenge", "Summary")
    var selectedSection by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    var evalLoading by remember { mutableStateOf(false) }
    var evalError by remember { mutableStateOf<String?>(null) }

    var enToBnInput by remember(lesson.level) { mutableStateOf("") }
    var enToBnEval by remember(lesson.level) { mutableStateOf<TranslationEvaluation?>(null) }

    var bnToEnInput by remember(lesson.level) { mutableStateOf("") }
    var bnToEnEval by remember(lesson.level) { mutableStateOf<TranslationEvaluation?>(null) }

    val grammarAnswers = remember(lesson.level) { mutableStateMapOfInt() }
    val challengeAnswers = remember(lesson.level) { mutableStateMapOfInt() }

    val builtWords = remember(lesson.level) { mutableStateListOf<String>() }
    var sentenceChecked by remember(lesson.level) { mutableStateOf<Boolean?>(null) }

    var sessionSaved by remember(lesson.level) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "10-Minute English Practice",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onOpenAiAssistant) { Text("AI Tutor") }
            }
        }

        if (!viewModel.isLiveGeminiConnected) {
            item { FirebaseAiNoticeBanner() }
        }

        // Level Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EnglishContentBank.levels) { lvl ->
                    FilterChip(
                        selected = profile.englishLevel == lvl,
                        onClick = { viewModel.updateProfile { it.copy(englishLevel = lvl) } },
                        label = { Text(lvl) },
                        modifier = Modifier.testTag("english_level_$lvl")
                    )
                }
            }
        }

        // 5-Activity Lesson Stepper
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sections.indices.toList()) { idx ->
                    FilterChip(
                        selected = selectedSection == idx,
                        onClick = { selectedSection = idx },
                        label = { Text(sections[idx]) },
                        modifier = Modifier.testTag("english_step_$idx")
                    )
                }
            }
        }

        when (selectedSection) {
            0 -> {
                // A. English to Bangla Translation
                val prompt = lesson.enToBn.first()
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Translate into Bangla", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(prompt.sourceText, style = MaterialTheme.typography.titleLarge)
                            OutlinedTextField(
                                value = enToBnInput,
                                onValueChange = { enToBnInput = it },
                                label = { Text("আপনার বাংলা অনুবাদ লিখুন") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("en_to_bn_input"),
                                minLines = 2
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        evalLoading = true
                                        evalError = null
                                        try {
                                            enToBnEval = viewModel.evaluateTranslation(
                                                sourceText = prompt.sourceText,
                                                expectedTranslation = prompt.expectedTranslation,
                                                userAnswer = enToBnInput,
                                                direction = "EN_TO_BN",
                                                grammarHintBn = prompt.grammarHintBn
                                            )
                                        } catch (e: kotlinx.coroutines.CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            evalError = e.localizedMessage ?: "AI evaluation failed. Please try again."
                                        } finally {
                                            evalLoading = false
                                        }
                                    }
                                },
                                enabled = !evalLoading,
                                modifier = Modifier.fillMaxWidth().testTag("check_en_to_bn_button")
                            ) {
                                Text(if (evalLoading) "Evaluating with Gemini..." else "Check Translation")
                            }
                            if (evalError != null) {
                                Text(evalError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                enToBnEval?.let { eval ->
                    item { EvaluationResultCard(eval) }
                }
            }

            1 -> {
                // B. Bangla to English Translation
                val prompt = lesson.bnToEn.first()
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Translate into English", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(prompt.sourceText, style = MaterialTheme.typography.titleLarge)
                            OutlinedTextField(
                                value = bnToEnInput,
                                onValueChange = { bnToEnInput = it },
                                label = { Text("Write your English translation") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bn_to_en_input"),
                                minLines = 2
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        evalLoading = true
                                        evalError = null
                                        try {
                                            bnToEnEval = viewModel.evaluateTranslation(
                                                sourceText = prompt.sourceText,
                                                expectedTranslation = prompt.expectedTranslation,
                                                userAnswer = bnToEnInput,
                                                direction = "BN_TO_EN",
                                                grammarHintBn = prompt.grammarHintBn
                                            )
                                        } catch (e: kotlinx.coroutines.CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            evalError = e.localizedMessage ?: "AI evaluation failed. Please try again."
                                        } finally {
                                            evalLoading = false
                                        }
                                    }
                                },
                                enabled = !evalLoading,
                                modifier = Modifier.fillMaxWidth().testTag("check_bn_to_en_button")
                            ) {
                                Text(if (evalLoading) "Evaluating with Gemini..." else "Evaluate English")
                            }
                            if (evalError != null) {
                                Text(evalError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                bnToEnEval?.let { eval ->
                    item { EvaluationResultCard(eval) }
                }
            }

            2 -> {
                // C. Grammar Practice
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Today's Grammar Topic", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(lesson.grammarTopic, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(lesson.grammarExplanationBn, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                items(lesson.grammarQuestions.indices.toList()) { idx ->
                    val q = lesson.grammarQuestions[idx]
                    val chosen = grammarAnswers[idx]
                    McqCard(
                        index = idx + 1,
                        question = q.question,
                        options = q.options,
                        selectedIndex = chosen,
                        correctIndex = q.correctIndex,
                        explanationBn = q.explanationBn,
                        onSelect = { grammarAnswers[idx] = it }
                    )
                }
            }

            3 -> {
                // D. Sentence Building
                val item = lesson.sentenceBuildItems.first()
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Arrange the words into a correct sentence", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(item.promptBn, style = MaterialTheme.typography.titleMedium)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (builtWords.isEmpty()) "Tap words below to build your sentence..." else builtWords.joinToString(" "),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.shuffledWords.forEach { word ->
                                    val used = builtWords.contains(word)
                                    AssistChip(
                                        onClick = {
                                            if (used) builtWords.remove(word) else builtWords.add(word)
                                            sentenceChecked = null
                                        },
                                        label = { Text(word) },
                                        enabled = !used
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        builtWords.clear()
                                        sentenceChecked = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Reset") }
                                Button(
                                    onClick = {
                                        sentenceChecked = builtWords.joinToString(" ").equals(item.correctSentence, ignoreCase = true)
                                    },
                                    modifier = Modifier.weight(1f).testTag("check_sentence_button")
                                ) { Text("Check Sentence") }
                            }

                            sentenceChecked?.let { ok ->
                                Surface(
                                    color = if (ok) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (ok) "সঠিক হয়েছে! Excellent sentence structure." else "প্রায় হয়েছে! Correct: ${item.correctSentence}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(item.explanationBn, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // E. Daily Challenge
                item {
                    SectionHeader(title = "Daily Challenge", subtitle = "Earn bonus points on 3 quick questions")
                }
                items(lesson.dailyChallenge.indices.toList()) { idx ->
                    val q = lesson.dailyChallenge[idx]
                    val chosen = challengeAnswers[idx]
                    McqCard(
                        index = idx + 1,
                        question = q.question,
                        options = q.options,
                        selectedIndex = chosen,
                        correctIndex = q.correctIndex,
                        explanationBn = q.explanationBn,
                        onSelect = { challengeAnswers[idx] = it }
                    )
                }
            }

            else -> {
                // Session Summary
                item {
                    val totalQ = 2 + lesson.grammarQuestions.size + 1 + lesson.dailyChallenge.size
                    val correctQ =
                        (if ((enToBnEval?.scoreOutOf10 ?: 0) >= 6) 1 else 0) +
                            (if ((bnToEnEval?.scoreOutOf10 ?: 0) >= 6) 1 else 0) +
                            lesson.grammarQuestions.indices.count { grammarAnswers[it] == lesson.grammarQuestions[it].correctIndex } +
                            (if (sentenceChecked == true) 1 else 0) +
                            lesson.dailyChallenge.indices.count { challengeAnswers[it] == lesson.dailyChallenge[it].correctIndex }
                    val score10 = ((correctQ.toFloat() / totalQ.coerceAtLeast(1)) * 10f).toInt().coerceIn(0, 10)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Session Summary", style = MaterialTheme.typography.headlineSmall)
                            Text("Level: ${lesson.level}")
                            Text("Total Questions: $totalQ")
                            Text("Correct Answers: $correctQ")
                            Text("Mistakes / Pending: ${totalQ - correctQ}")
                            Text("Final Score: $score10 / 10", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Grammar Learned: ${lesson.grammarTopic}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    viewModel.saveEnglishSession(
                                        level = lesson.level,
                                        totalQuestions = totalQ,
                                        correctAnswers = correctQ,
                                        scoreOutOf10 = score10,
                                        grammarTopic = lesson.grammarTopic
                                    )
                                    sessionSaved = true
                                },
                                modifier = Modifier.fillMaxWidth().testTag("save_english_session_button")
                            ) {
                                Text(if (sessionSaved) "Session Saved Locally ✓" else "Save Today's Practice Session")
                            }
                            if (sessions.isNotEmpty()) {
                                Text(
                                    text = "Completed sessions logged: ${sessions.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvaluationResultCard(eval: TranslationEvaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Evaluation Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = "${eval.scoreOutOf10} / 10",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text("1. Your Answer: ${eval.userAnswer}", style = MaterialTheme.typography.bodyMedium)
            Text("2. Reference Answer: ${eval.correctAnswer}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("3. Notes / Mistakes: ${eval.mistakesFound.joinToString(" ")}", style = MaterialTheme.typography.bodySmall)
            Text("4. Grammar Explanation (Bangla): ${eval.grammarExplanationBn}", style = MaterialTheme.typography.bodySmall)
            Text("5. Alternative: ${eval.alternatives.joinToString(" | ")}", style = MaterialTheme.typography.bodySmall)
            Text(eval.encouragement, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun McqCard(
    index: Int,
    question: String,
    options: List<String>,
    selectedIndex: Int?,
    correctIndex: Int,
    explanationBn: String,
    onSelect: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Q$index. $question", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            options.forEachIndexed { idx, opt ->
                val isSelected = selectedIndex == idx
                val isCorrect = idx == correctIndex
                val bg = when {
                    selectedIndex == null -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Surface(
                    onClick = { onSelect(idx) },
                    shape = RoundedCornerShape(12.dp),
                    color = bg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
            if (selectedIndex != null) {
                Text(
                    text = explanationBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun mutableStateMapOfInt() = androidx.compose.runtime.mutableStateMapOf<Int, Int>()
