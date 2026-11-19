package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.NewsRssService
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.theme.NotoSansBengaliFamily
import com.example.viewmodel.DailyPathViewModel

private val newsCategories = listOf(
    "All", "National", "Politics", "Economy", "Education", "Climate", "Science & Tech", "Sports", "World Affairs"
)

@Composable
fun NewsScreen(
    viewModel: DailyPathViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val articles by viewModel.newsArticles.collectAsStateWithLifecycle()
    val isLoading by viewModel.newsLoading.collectAsStateWithLifecycle()
    val newsError by viewModel.newsError.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val summaries by viewModel.newsSummaries.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Bangladesh, 1 = International, 2 = Bookmarked
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedChannel by remember { mutableStateOf("All") }

    val filtered = remember(articles, selectedTab, searchQuery, selectedCategory, selectedChannel) {
        articles.filter { item ->
            val tabMatch = when (selectedTab) {
                0 -> item.region == "BANGLADESH"
                1 -> item.region == "INTERNATIONAL"
                else -> item.bookmarked
            }
            val channelMatch = selectedTab != 0 || selectedChannel == "All" ||
                item.source.equals(selectedChannel, ignoreCase = true)
            val catMatch = selectedCategory == "All" || item.category.contains(selectedCategory, ignoreCase = true)
            val queryMatch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.summary.contains(searchQuery, ignoreCase = true) ||
                item.source.contains(searchQuery, ignoreCase = true)
            tabMatch && channelMatch && catMatch && queryMatch
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text("Daily News", style = MaterialTheme.typography.titleLarge)
                    Text("Live Bangladesh & World RSS feeds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(
                onClick = { viewModel.refreshNews() },
                modifier = Modifier.testTag("refresh_news_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh news")
                }
            }
        }

        val (bdCount, worldCount, savedCount) = remember(articles) {
            Triple(
                articles.count { it.region == "BANGLADESH" },
                articles.count { it.region == "INTERNATIONAL" },
                articles.count { it.bookmarked }
            )
        }
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Bangladesh ($bdCount)") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("World ($worldCount)") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Saved ($savedCount)") })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isOnline) {
                item { OfflineStatusBanner(isOnline = false) }
            }

            if (newsError != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = newsError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search headlines or sources") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("news_search_input")
                )
            }

            if (selectedTab == 0) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(NewsRssService.bangladeshChannels) { ch ->
                            FilterChip(
                                selected = selectedChannel == ch,
                                onClick = { selectedChannel = ch },
                                label = { Text(ch) },
                                modifier = Modifier.testTag("bd_channel_$ch")
                            )
                        }
                    }
                }
            }

            item(key = "news_categories") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(newsCategories, key = { it }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            if (filtered.isEmpty() && !isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("No articles match your current filter.", style = MaterialTheme.typography.titleSmall)
                            Text("DailyPath never generates fictional news. Tap refresh to fetch the latest live RSS articles.", style = MaterialTheme.typography.bodySmall)
                            Button(onClick = { viewModel.refreshNews() }) { Text("Refresh Live News") }
                        }
                    }
                }
            }

            items(filtered, key = { it.id }) { article ->
                val aiSummary = summaries[article.id]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (article.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = article.imageUrl,
                                contentDescription = article.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${article.source} • ${article.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.toggleNewsBookmark(article.id, !article.bookmarked) }) {
                                Icon(
                                    imageVector = if (article.bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark article",
                                    tint = if (article.bookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = NotoSansBengaliFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(article.publishedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = NotoSansBengaliFamily
                        )

                        if (aiSummary != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = aiSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { viewModel.summarizeNews(article) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Summarize")
                            }
                            Button(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Full Article")
                            }
                        }
                    }
                }
            }

            if (!viewModel.isLiveGeminiConnected) {
                item { FirebaseAiNoticeBanner() }
            }
        }
    }
}
