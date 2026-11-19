package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPdfEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfReaderScreen(
    pdf: UserPdfEntity,
    todayDateKey: String,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit
) {
    BackHandler { onBack() }

    val safePage = pdf.currentPage.coerceIn(1, pdf.totalPages.coerceAtLeast(1))
    val bookmarkedPages = remember(pdf.bookmarkedPagesCsv) {
        pdf.bookmarkedPagesCsv.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }
    val isBookmarked = safePage in bookmarkedPages
    val readToday = if (pdf.lastReadDateKey == todayDateKey) pdf.pagesReadToday else 0

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var renderError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf(safePage.toString()) }

    LaunchedEffect(pdf.localFilePath, safePage) {
        isLoading = true
        renderError = null
        val result = withContext(Dispatchers.IO) {
            try {
                val file = File(pdf.localFilePath)
                if (!file.exists()) {
                    return@withContext Result.failure<Bitmap>(IllegalStateException("PDF file no longer exists on device."))
                }
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        val pageIdx = (safePage - 1).coerceIn(0, (renderer.pageCount - 1).coerceAtLeast(0))
                        renderer.openPage(pageIdx).use { page ->
                            val targetWidth = 900
                            val targetHeight = ((page.height.toFloat() / page.width.toFloat()) * targetWidth).toInt().coerceAtLeast(400)
                            val bmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)
                            bmp.eraseColor(AndroidColor.WHITE)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            Result.success(bmp)
                        }
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        result.onSuccess { renderedBitmap = it }
            .onFailure { renderError = it.localizedMessage ?: "Unable to render PDF page." }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pdf.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Page $safePage of ${pdf.totalPages} • Today: $readToday pages (Auto-paused here)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row {
                IconButton(onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f) }) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out")
                }
                IconButton(onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(2.5f) }) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in")
                }
                IconButton(onClick = { onToggleBookmark(safePage) }) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark page",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // PDF Page Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                renderError != null -> Text(
                    text = renderError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                renderedBitmap != null -> {
                    val imageBitmap = remember(renderedBitmap) { renderedBitmap!!.asImageBitmap() }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val widthDp = (340f * zoomScale).dp
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "PDF page $safePage",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.width(widthDp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Bottom Page Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (safePage > 1) onPageChanged(safePage - 1) },
                enabled = safePage > 1,
                modifier = Modifier.weight(1f)
            ) { Text("Prev") }

            Surface(
                onClick = {
                    jumpPageInput = safePage.toString()
                    showJumpDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "$safePage / ${pdf.totalPages}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            Button(
                onClick = { if (safePage < pdf.totalPages) onPageChanged(safePage + 1) },
                enabled = safePage < pdf.totalPages,
                modifier = Modifier.weight(1f).testTag("pdf_next_page_button")
            ) { Text(if (safePage < pdf.totalPages) "Next" else "Finished ✓") }
        }
    }

    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("Jump to Page") },
            text = {
                OutlinedTextField(
                    value = jumpPageInput,
                    onValueChange = { jumpPageInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Page (1 - ${pdf.totalPages})") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val target = jumpPageInput.toIntOrNull()?.coerceIn(1, pdf.totalPages)
                    if (target != null) onPageChanged(target)
                    showJumpDialog = false
                }) { Text("Go") }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) { Text("Cancel") }
            }
        )
    }
}
