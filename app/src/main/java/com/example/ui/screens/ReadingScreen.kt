package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.content.LiteratureBook
import com.example.data.content.LiteratureContentBank
import com.example.ui.components.PdfReaderScreen
import com.example.ui.components.SectionHeader
import com.example.ui.theme.NotoSansBengaliFamily
import com.example.viewmodel.DailyPathViewModel

@Composable
fun ReadingScreen(viewModel: DailyPathViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val progressMap by viewModel.readingProgress.collectAsStateWithLifecycle()
    val allPdfs by viewModel.userPdfs.collectAsStateWithLifecycle()
    val readingPdfs = remember(allPdfs) { allPdfs.filter { it.section == "READING" } }

    var selectedCategory by remember { mutableStateOf("All") }
    var openBook by remember { mutableStateOf<LiteratureBook?>(null) }
    var openPdfId by remember { mutableStateOf<String?>(null) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importUserPdf(uri, "READING") { msg -> uploadStatus = msg }
        }
    }

    val activePdf = readingPdfs.firstOrNull { it.id == openPdfId }
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

    if (openBook != null) {
        val book = openBook!!
        BackHandler { openBook = null }
        BengaliBookReader(
            book = book,
            currentPage = progressMap[book.id]?.currentPage ?: 1,
            pagesReadToday = if (progressMap[book.id]?.lastReadDateKey == viewModel.todayKey) {
                progressMap[book.id]?.pagesReadToday ?: 0
            } else 0,
            bookmarkedPages = progressMap[book.id]?.bookmarkedPagesCsv
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet() ?: emptySet(),
            fontSizeSp = profile.readerFontSizeSp,
            lineHeightMultiplier = profile.readerLineHeightMultiplier,
            darkPaper = profile.readerDarkMode,
            onBack = { openBook = null },
            onPageChanged = { newPage -> viewModel.updateReadingPage(book.id, newPage, false) },
            onToggleBookmark = { page -> viewModel.updateReadingPage(book.id, page, true) },
            onChangeFontSize = { delta ->
                viewModel.updateProfile { it.copy(readerFontSizeSp = (it.readerFontSizeSp + delta).coerceIn(14f, 28f)) }
            },
            onToggleLineSpacing = {
                viewModel.updateProfile {
                    val next = if (it.readerLineHeightMultiplier >= 1.85f) 1.4f else it.readerLineHeightMultiplier + 0.2f
                    it.copy(readerLineHeightMultiplier = next)
                }
            },
            onToggleDarkPaper = {
                viewModel.updateProfile { it.copy(readerDarkMode = !it.readerDarkMode) }
            }
        )
        return
    }

    val totalPagesToday = remember(progressMap, readingPdfs, viewModel.todayKey) {
        progressMap.values.filter { it.lastReadDateKey == viewModel.todayKey }.sumOf { it.pagesReadToday } +
            readingPdfs.filter { it.lastReadDateKey == viewModel.todayKey }.sumOf { it.pagesReadToday }
    }
    val totalPagesAllTime = remember(progressMap, readingPdfs) {
        progressMap.values.sumOf { it.totalPagesRead } + readingPdfs.sumOf { it.totalPagesRead }
    }

    val filteredBooks = remember(selectedCategory) {
        if (selectedCategory == "All") LiteratureContentBank.books
        else LiteratureContentBank.books.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Bengali Literature & Personal PDFs", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Read as many pages as you like — your exact page pauses automatically and resumes next day.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Unlimited Reading Tracker + Upload PDF CTA
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AsyncImage(
                        model = R.drawable.img_bengali_heritage_1795091367506,
                        contentDescription = "Bengali literature header",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Pages Read Today: $totalPagesToday (No Limit)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "All-Time: $totalPagesAllTime",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.fillMaxWidth().testTag("upload_reading_pdf_button")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upload Personal PDF Book")
                    }
                    if (uploadStatus != null) {
                        Text(
                            text = uploadStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Personal Uploaded PDFs Section
        if (readingPdfs.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "My Uploaded PDFs",
                    subtitle = "Automatically resumes from your paused page"
                )
            }
            items(readingPdfs, key = { it.id }) { pdf ->
                val pdfToday = if (pdf.lastReadDateKey == viewModel.todayKey) pdf.pagesReadToday else 0
                Card(
                    onClick = { openPdfId = pdf.id },
                    modifier = Modifier.fillMaxWidth().testTag("reading_pdf_${pdf.id}"),
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

        // Built-in Literature Categories
        item {
            SectionHeader(title = "Classic Bengali Literature")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LiteratureContentBank.categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        items(filteredBooks, key = { it.id }) { book ->
            val prog = progressMap[book.id]
            val currentPage = prog?.currentPage ?: 1
            Card(
                onClick = { openBook = book },
                modifier = Modifier.fillMaxWidth().testTag("book_card_${book.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = book.titleBn,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = NotoSansBengaliFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${book.authorBn} (${book.authorEn})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text("${book.category} • ${book.licenseNote}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Paused at Page $currentPage of ${book.pages.size}", style = MaterialTheme.typography.labelMedium)
                        Text("Resume →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BengaliBookReader(
    book: LiteratureBook,
    currentPage: Int,
    pagesReadToday: Int,
    bookmarkedPages: Set<Int>,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
    darkPaper: Boolean,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onChangeFontSize: (Float) -> Unit,
    onToggleLineSpacing: () -> Unit,
    onToggleDarkPaper: () -> Unit
) {
    val safePage = currentPage.coerceIn(1, book.pages.size)
    val pageText = book.pages[safePage - 1]
    val isBookmarked = safePage in bookmarkedPages

    val bgColor = if (darkPaper) Color(0xFF101B16) else Color(0xFFFAF6EC)
    val fgColor = if (darkPaper) Color(0xFFEAECE6) else Color(0xFF1B241E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to library", tint = fgColor)
            }
            Row {
                IconButton(onClick = { onChangeFontSize(-2f) }) {
                    Icon(Icons.Default.TextDecrease, contentDescription = "Decrease font", tint = fgColor)
                }
                IconButton(onClick = { onChangeFontSize(2f) }) {
                    Icon(Icons.Default.TextIncrease, contentDescription = "Increase font", tint = fgColor)
                }
                IconButton(onClick = onToggleDarkPaper) {
                    Icon(
                        if (darkPaper) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle reader theme",
                        tint = fgColor
                    )
                }
                IconButton(onClick = { onToggleBookmark(safePage) }) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark page",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else fgColor
                    )
                }
            }
        }

        Text(
            text = book.titleBn,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = NotoSansBengaliFamily,
            color = fgColor
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Page $safePage / ${book.pages.size} • Today: $pagesReadToday pages (Auto-paused)",
                style = MaterialTheme.typography.labelMedium,
                color = fgColor.copy(alpha = 0.75f)
            )
            Surface(
                onClick = onToggleLineSpacing,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Spacing ${"%.1f".format(lineHeightMultiplier)}x",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = pageText,
                fontFamily = NotoSansBengaliFamily,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
                color = fgColor
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { if (safePage > 1) onPageChanged(safePage - 1) },
                enabled = safePage > 1,
                modifier = Modifier.weight(1f)
            ) { Text("Previous Page") }
            Button(
                onClick = { if (safePage < book.pages.size) onPageChanged(safePage + 1) },
                enabled = safePage < book.pages.size,
                modifier = Modifier.weight(1f).testTag("reader_next_page_button")
            ) { Text(if (safePage < book.pages.size) "Next Page" else "Completed ✓") }
        }
    }
}
