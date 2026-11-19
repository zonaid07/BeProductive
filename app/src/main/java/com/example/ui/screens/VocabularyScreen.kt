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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.VocabularyContentBank
import com.example.viewmodel.DailyPathViewModel

@Composable
fun VocabularyScreen(viewModel: DailyPathViewModel) {
    val progressMap by viewModel.vocabProgress.collectAsStateWithLifecycle()
    val dailyWords = remember { VocabularyContentBank.dailyTenWords() }
    val allWords = remember { VocabularyContentBank.allWords }

    val modes = listOf("Daily 10", "Flashcards", "MCQ Quiz", "Fill Blanks", "Match EN-BN", "Spelling", "Sentence", "Revision")
    var selectedMode by remember { mutableIntStateOf(0) }

    // Interactive practice states
    var flashIndex by remember { mutableIntStateOf(0) }
    var flashFlipped by remember { mutableStateOf(false) }

    var quizIndex by remember { mutableIntStateOf(0) }
    var quizFeedback by remember { mutableStateOf<String?>(null) }

    var blankIndex by remember { mutableIntStateOf(0) }
    var blankInput by remember { mutableStateOf("") }
    var blankFeedback by remember { mutableStateOf<String?>(null) }

    var spellIndex by remember { mutableIntStateOf(0) }
    var spellInput by remember { mutableStateOf("") }
    var spellFeedback by remember { mutableStateOf<String?>(null) }

    var sentenceIndex by remember { mutableIntStateOf(0) }
    var sentenceInput by remember { mutableStateOf("") }
    var sentenceFeedback by remember { mutableStateOf<String?>(null) }

    var matchSelectedEn by remember { mutableStateOf<String?>(null) }
    var matchFeedback by remember { mutableStateOf<String?>(null) }

    var customCollectionInput by remember { mutableStateOf("") }

    val learnedCount = allWords.count { progressMap[it.id]?.learned == true }
    val bookmarkedCount = allWords.count { progressMap[it.id]?.bookmarked == true }
    val revisionDue = allWords.filter {
        val p = progressMap[it.id]
        p != null && (p.mistakesCount > 0 || p.bookmarked)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Vocabulary Builder", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Learned: $learnedCount/${allWords.size} • Bookmarked: $bookmarkedCount • Revision Due: ${revisionDue.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mode Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(modes.indices.toList()) { idx ->
                    FilterChip(
                        selected = selectedMode == idx,
                        onClick = { selectedMode = idx },
                        label = { Text(modes[idx]) },
                        modifier = Modifier.testTag("vocab_mode_$idx")
                    )
                }
            }
        }

        when (selectedMode) {
            0 -> {
                // Daily 10 Words List
                items(dailyWords, key = { it.id }) { word ->
                    val prog = progressMap[word.id]
                    val learned = prog?.learned == true
                    val bookmarked = prog?.bookmarked == true
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${word.word} — ${word.banglaMeaning}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${word.pronunciation} • ${word.partOfSpeech}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(onClick = { viewModel.toggleVocabBookmark(word.id) }) {
                                        Icon(
                                            imageVector = if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark word",
                                            tint = if (bookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { viewModel.markVocabLearned(word.id, !learned) }) {
                                        Icon(
                                            imageVector = if (learned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Mark learned",
                                            tint = if (learned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                            Text(word.definitionEn, style = MaterialTheme.typography.bodySmall)
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("\"${word.exampleEn}\"", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text(word.exampleBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (!prog?.customCollection.isNullOrBlank()) {
                                Text(
                                    text = "Collection: ${prog?.customCollection}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Flashcards
                val word = dailyWords[flashIndex % dailyWords.size]
                item {
                    Card(
                        onClick = { flashFlipped = !flashFlipped },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Card ${flashIndex + 1} / ${dailyWords.size}", style = MaterialTheme.typography.labelMedium)
                            Text(word.word, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${word.pronunciation} (${word.partOfSpeech})", style = MaterialTheme.typography.bodyMedium)
                            if (flashFlipped) {
                                Spacer(Modifier.height(8.dp))
                                Text(word.banglaMeaning, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                Text(word.definitionEn, style = MaterialTheme.typography.bodyMedium)
                                Text("\"${word.exampleEn}\"", style = MaterialTheme.typography.bodySmall)
                                Text(word.exampleBn, style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Tap card to reveal Bangla meaning & example", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.recordVocabPracticeResult(word.id, false)
                                flashFlipped = false
                                flashIndex = (flashIndex + 1) % dailyWords.size
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Needs Review") }
                        Button(
                            onClick = {
                                viewModel.recordVocabPracticeResult(word.id, true)
                                flashFlipped = false
                                flashIndex = (flashIndex + 1) % dailyWords.size
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("I Know This ✓") }
                    }
                }
            }

            2 -> {
                // MCQ Quiz
                val target = dailyWords[quizIndex % dailyWords.size]
                item {
                    val options = remember(target.id) {
                        (dailyWords.filter { it.id != target.id }.shuffled().take(3) + target).shuffled()
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Choose the Bangla meaning of:", style = MaterialTheme.typography.labelMedium)
                            Text(target.word, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            options.forEach { opt ->
                                OutlinedButton(
                                    onClick = {
                                        val ok = opt.id == target.id
                                        viewModel.recordVocabPracticeResult(target.id, ok)
                                        quizFeedback = if (ok) "সঠিক! ${target.word} = ${target.banglaMeaning}" else "ভুল হয়েছে। সঠিক অর্থ: ${target.banglaMeaning}"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(opt.banglaMeaning) }
                            }
                            quizFeedback?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium) }
                            Button(
                                onClick = {
                                    quizFeedback = null
                                    quizIndex = (quizIndex + 1) % dailyWords.size
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Next Question") }
                        }
                    }
                }
            }

            3 -> {
                // Fill-in-the-blanks
                val word = dailyWords[blankIndex % dailyWords.size]
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Fill in the blank", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(word.blankSentence, style = MaterialTheme.typography.titleMedium)
                            Text("Hint (Bangla): ${word.banglaMeaning}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = blankInput,
                                onValueChange = { blankInput = it },
                                label = { Text("Type the missing English word") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val ok = blankInput.trim().equals(word.word, ignoreCase = true)
                                    viewModel.recordVocabPracticeResult(word.id, ok)
                                    blankFeedback = if (ok) "চমৎকার! Correct word: ${word.word}" else "সঠিক শব্দ: ${word.word}"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Check Answer") }
                            blankFeedback?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                            OutlinedButton(
                                onClick = {
                                    blankInput = ""
                                    blankFeedback = null
                                    blankIndex = (blankIndex + 1) % dailyWords.size
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Next Sentence") }
                        }
                    }
                }
            }

            4 -> {
                // Match English with Bangla
                item {
                    val pairSet = remember { dailyWords.take(4) }
                    val shuffledBn = remember { pairSet.shuffled() }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Match English Words with Bangla Meanings", style = MaterialTheme.typography.titleMedium)
                            Text("1. Tap an English word, then tap its Bangla meaning.", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    pairSet.forEach { w ->
                                        FilterChip(
                                            selected = matchSelectedEn == w.id,
                                            onClick = { matchSelectedEn = w.id },
                                            label = { Text(w.word) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    shuffledBn.forEach { w ->
                                        OutlinedButton(
                                            onClick = {
                                                val sel = matchSelectedEn
                                                if (sel != null) {
                                                    val ok = sel == w.id
                                                    viewModel.recordVocabPracticeResult(sel, ok)
                                                    matchFeedback = if (ok) "Matched! ${w.word} = ${w.banglaMeaning}" else "Try again!"
                                                    matchSelectedEn = null
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text(w.banglaMeaning, maxLines = 1) }
                                    }
                                }
                            }
                            matchFeedback?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
            }

            5 -> {
                // Spelling Test
                val word = dailyWords[spellIndex % dailyWords.size]
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Spelling Test", style = MaterialTheme.typography.titleMedium)
                            Text("Bangla Meaning: ${word.banglaMeaning}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Text("Definition: ${word.definitionEn}", style = MaterialTheme.typography.bodySmall)
                            Text("Pronunciation: ${word.pronunciation}", style = MaterialTheme.typography.labelMedium)
                            OutlinedTextField(
                                value = spellInput,
                                onValueChange = { spellInput = it },
                                label = { Text("Spell the English word") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val ok = spellInput.trim().equals(word.word, ignoreCase = true)
                                    viewModel.recordVocabPracticeResult(word.id, ok)
                                    spellFeedback = if (ok) "Perfect spelling! (${word.word})" else "Correct spelling: ${word.word}"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Check Spelling") }
                            spellFeedback?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                            OutlinedButton(
                                onClick = {
                                    spellInput = ""
                                    spellFeedback = null
                                    spellIndex = (spellIndex + 1) % dailyWords.size
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Next Word") }
                        }
                    }
                }
            }

            6 -> {
                // Sentence Making Exercise
                val word = dailyWords[sentenceIndex % dailyWords.size]
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Sentence Making Exercise", style = MaterialTheme.typography.titleMedium)
                            Text("Use '${word.word}' (${word.banglaMeaning}) in your own sentence:", style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = sentenceInput,
                                onValueChange = { sentenceInput = it },
                                label = { Text("Write a complete English sentence") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val contains = sentenceInput.contains(word.word, ignoreCase = true) && sentenceInput.trim().split(" ").size >= 4
                                    viewModel.recordVocabPracticeResult(word.id, contains)
                                    sentenceFeedback = if (contains) {
                                        "দারুণ বাক্য! Model example: ${word.exampleEn}"
                                    } else {
                                        "Make sure your sentence includes '${word.word}' and at least 4 words. Example: ${word.exampleEn}"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Check My Sentence") }
                            sentenceFeedback?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                            OutlinedButton(
                                onClick = {
                                    sentenceInput = ""
                                    sentenceFeedback = null
                                    sentenceIndex = (sentenceIndex + 1) % dailyWords.size
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Next Word") }
                        }
                    }
                }
            }

            else -> {
                // Smart Spaced Repetition Revision & Custom Collections
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Smart Spaced Repetition & Collections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Difficult or bookmarked words automatically return here for review.", style = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(
                                value = customCollectionInput,
                                onValueChange = { customCollectionInput = it },
                                label = { Text("Custom collection tag (e.g., IELTS, Daily)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                val listToShow = revisionDue.ifEmpty { dailyWords.take(3) }
                items(listToShow, key = { it.id }) { word ->
                    val prog = progressMap[word.id]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${word.word} — ${word.banglaMeaning}", fontWeight = FontWeight.Bold)
                                Text("Reviews: ${prog?.reviewsCount ?: 0} • Mistakes: ${prog?.mistakesCount ?: 0}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (customCollectionInput.isNotBlank()) {
                                OutlinedButton(onClick = { viewModel.assignVocabCollection(word.id, customCollectionInput) }) {
                                    Text("Tag")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
