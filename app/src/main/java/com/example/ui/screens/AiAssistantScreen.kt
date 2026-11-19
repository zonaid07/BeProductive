package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.NotoSansBengaliFamily
import com.example.viewmodel.DailyPathViewModel

@Composable
fun AiAssistantScreen(
    viewModel: DailyPathViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val loading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val aiError by viewModel.aiError.collectAsStateWithLifecycle()

    val topics = listOf(
        "English Grammar",
        "Vocabulary",
        "Translation",
        "Bengali Literature",
        "Islamic History",
        "General Knowledge"
    )
    val quickPrompts = listOf(
        "Present Perfect Tense সহজ বাংলায় বুঝিয়ে দাও",
        "Translate: নিয়মিত অভ্যাস মানুষকে সফল করে",
        "ইসলামের স্বর্ণযুগ সম্পর্কে সংক্ষেপে বলো",
        "রবীন্দ্রনাথের কাবুলিওয়ালা গল্পের মূল ভাব কী?"
    )

    var selectedTopic by remember { mutableStateOf(topics.first()) }
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Personal AI Teacher", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Live Gemini AI",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Live Gemini • Simple Bangla & English explanations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(
                onClick = { viewModel.clearAiChat() },
                modifier = Modifier.testTag("ai_reset_chat_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "New conversation")
            }
        }

        // Topic Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topics) { topic ->
                FilterChip(
                    selected = selectedTopic == topic,
                    onClick = { selectedTopic = topic },
                    label = { Text(topic) }
                )
            }
        }

        // Quick Starter Prompts (shown when conversation is fresh)
        if (messages.size <= 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { sample ->
                    AssistChip(
                        onClick = { viewModel.askAiAssistant(sample, selectedTopic) },
                        label = { Text(sample, fontFamily = NotoSansBengaliFamily, maxLines = 1) }
                    )
                }
            }
        }

        // Scrollable Multi-Turn Chat Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        val display = if (msg.text.isBlank() && loading) "উত্তর তৈরি হচ্ছে..." else msg.text
                        Text(
                            text = display,
                            fontFamily = NotoSansBengaliFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (aiError != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = aiError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Message Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("বাংলা বা ইংরেজিতে প্রশ্ন লিখুন...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                maxLines = 3,
                enabled = !loading
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (promptInput.isNotBlank() && !loading) {
                        viewModel.askAiAssistant(promptInput, selectedTopic)
                        promptInput = ""
                    }
                },
                enabled = !loading && promptInput.isNotBlank(),
                modifier = Modifier.testTag("ai_send_button")
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send question",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
