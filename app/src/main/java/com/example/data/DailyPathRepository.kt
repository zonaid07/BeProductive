package com.example.data

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.data.local.CachedNewsEntity
import com.example.data.local.DailyPathDatabase
import com.example.data.local.DailyTaskEntity
import com.example.data.local.EnglishSessionEntity
import com.example.data.local.HistoryProgressEntity
import com.example.data.local.PrayerAlarmEntity
import com.example.data.local.ReadingProgressEntity
import com.example.data.local.UserPdfEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VocabProgressEntity
import com.example.prayer.PrayerAlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyPathRepository(
    private val context: Context,
    private val db: DailyPathDatabase,
    private val newsService: NewsRssService = NewsRssService(),
    val aiRepository: AiRepository = FirebaseAiRepository()
) {
    private val profileDao = db.userProfileDao()
    private val taskDao = db.dailyTaskDao()
    private val alarmDao = db.prayerAlarmDao()
    private val vocabDao = db.vocabProgressDao()
    private val readingDao = db.readingProgressDao()
    private val userPdfDao = db.userPdfDao()
    private val historyDao = db.historyProgressDao()
    private val newsDao = db.cachedNewsDao()
    private val englishDao = db.englishSessionDao()

    fun todayDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun observeProfile(): Flow<UserProfileEntity> =
        profileDao.observeProfile().map { it ?: UserProfileEntity() }

    fun observeTasksForDate(dateKey: String): Flow<List<DailyTaskEntity>> =
        taskDao.observeTasksForDate(dateKey)

    fun observeAllTasks(): Flow<List<DailyTaskEntity>> = taskDao.observeAllTasks()

    fun observePrayerAlarms(): Flow<List<PrayerAlarmEntity>> = alarmDao.observeAlarms()

    fun observeVocabProgress(): Flow<List<VocabProgressEntity>> = vocabDao.observeAll()

    fun observeReadingProgress(): Flow<List<ReadingProgressEntity>> = readingDao.observeAll()

    fun observeUserPdfs(): Flow<List<UserPdfEntity>> = userPdfDao.observeAllPdfs()

    fun observeHistoryProgress(): Flow<List<HistoryProgressEntity>> = historyDao.observeAll()

    fun observeNews(): Flow<List<CachedNewsEntity>> = newsDao.observeAllNews()

    fun observeEnglishSessions(): Flow<List<EnglishSessionEntity>> = englishDao.observeAllSessions()

    suspend fun ensureSeededForToday() = withContext(Dispatchers.IO) {
        val existingProfile = profileDao.getProfileOnce()
        if (existingProfile == null) {
            profileDao.upsertProfile(UserProfileEntity())
        }

        val existingAlarms = alarmDao.getAlarmsOnce()
        if (existingAlarms.isEmpty()) {
            val defaults = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Tahajjud").map { name ->
                PrayerAlarmEntity(
                    prayerName = name,
                    enabled = name != "Tahajjud",
                    reminderMinutesBefore = if (name == "Tahajjud") 0 else 10,
                    soundMode = "NOTIFICATION",
                    vibrationEnabled = true,
                    volumePercent = 80
                )
            }
            alarmDao.insertAllIgnore(defaults)
        }

        ensureDefaultTasksForDate(todayDateKey())
    }

    suspend fun hasFreshNewsCache(maxAgeMillis: Long = 15 * 60 * 1000L): Boolean = withContext(Dispatchers.IO) {
        val newest = newsDao.getLatestCachedAt() ?: return@withContext false
        (System.currentTimeMillis() - newest) < maxAgeMillis
    }

    suspend fun ensureDefaultTasksForDate(dateKey: String) {
        if (taskDao.countTasksForDate(dateKey) == 0) {
            val defaults = listOf(
                DailyTaskEntity("${dateKey}_1_prayer", dateKey, "1_prayer", "Complete daily prayers", "দৈনিক ৫ ওয়াক্ত নামাজ আদায়", "Prayer"),
                DailyTaskEntity("${dateKey}_2_english", dateKey, "2_english", "Complete 10-minute English practice", "১০ মিনিট ইংরেজি অনুশীলন", "English"),
                DailyTaskEntity("${dateKey}_3_vocab", dateKey, "3_vocab", "Learn 10 vocabulary words", "১০টি নতুন ইংরেজি শব্দ শেখা", "Vocabulary"),
                DailyTaskEntity("${dateKey}_4_news", dateKey, "4_news", "Read daily Bangladesh & World news", "দৈনিক বাংলাদেশ ও বিশ্ব সংবাদ পড়া", "News"),
                DailyTaskEntity("${dateKey}_5_reading", dateKey, "5_reading", "Daily Bengali literature / PDF reading", "দৈনিক বাংলা সাহিত্য ও ব্যক্তিগত PDF পাঠ", "Reading"),
                DailyTaskEntity("${dateKey}_6_history", dateKey, "6_history", "Study Islamic history / PDF notes", "ইসলামের ইতিহাস ও ব্যক্তিগত PDF অধ্যয়ন", "Knowledge")
            )
            taskDao.insertAllIgnore(defaults)
        }
    }

    suspend fun toggleTask(compositeId: String, completed: Boolean) {
        taskDao.setTaskCompleted(compositeId, completed)
    }

    suspend fun addCustomTask(dateKey: String, title: String, category: String, reminderTime: String) {
        val key = "custom_${System.currentTimeMillis()}"
        taskDao.upsertTask(
            DailyTaskEntity(
                compositeId = "${dateKey}_$key",
                dateKey = dateKey,
                taskKey = key,
                titleEn = title.trim(),
                titleBn = title.trim(),
                category = category,
                isCompleted = false,
                isCustom = true,
                reminderTime = reminderTime
            )
        )
    }

    suspend fun deleteTask(compositeId: String) {
        taskDao.deleteTask(compositeId)
    }

    suspend fun updateProfile(transform: (UserProfileEntity) -> UserProfileEntity) {
        val current = profileDao.getProfileOnce() ?: UserProfileEntity()
        val updated = transform(current)
        profileDao.upsertProfile(updated)
        val alarms = alarmDao.getAlarmsOnce()
        PrayerAlarmScheduler.scheduleAllAlarms(context, updated, alarms)
    }

    suspend fun updatePrayerAlarm(alarm: PrayerAlarmEntity) {
        alarmDao.upsertAlarm(alarm)
        val profile = profileDao.getProfileOnce() ?: UserProfileEntity()
        val alarms = alarmDao.getAlarmsOnce()
        PrayerAlarmScheduler.scheduleAllAlarms(context, profile, alarms)
    }

    fun testPrayerAlarm(alarm: PrayerAlarmEntity) {
        PrayerAlarmScheduler.triggerTestAlarm(context, alarm)
    }

    suspend fun updateVocabProgress(wordId: String, transform: (VocabProgressEntity) -> VocabProgressEntity) {
        val current = vocabDao.getByWordId(wordId) ?: VocabProgressEntity(wordId = wordId)
        vocabDao.upsert(transform(current))
    }

    /**
     * Unlimited daily reading progress with automatic pause & next-day resume.
     */
    suspend fun updateReadingProgress(bookId: String, page: Int, bookmarkToggle: Boolean = false) {
        val today = todayDateKey()
        val current = readingDao.getByBookId(bookId) ?: ReadingProgressEntity(bookId = bookId)
        val bookmarks = current.bookmarkedPagesCsv
            .split(",")
            .mapNotNull { it.toIntOrNull() }
            .toMutableSet()

        if (bookmarkToggle) {
            if (bookmarks.contains(page)) bookmarks.remove(page) else bookmarks.add(page)
        }

        val isSameDay = current.lastReadDateKey == today
        val pageAdvanced = page != current.currentPage
        val newPagesToday = when {
            !isSameDay -> 1
            pageAdvanced -> current.pagesReadToday + 1
            else -> current.pagesReadToday.coerceAtLeast(1)
        }
        val newTotal = if (pageAdvanced || current.totalPagesRead == 0) {
            current.totalPagesRead + 1
        } else {
            current.totalPagesRead
        }

        readingDao.upsert(
            current.copy(
                currentPage = page,
                pagesReadToday = newPagesToday,
                totalPagesRead = newTotal,
                lastReadDateKey = today,
                bookmarkedPagesCsv = bookmarks.sorted().joinToString(",")
            )
        )
    }

    suspend fun importUserPdf(uri: Uri, section: String): Result<UserPdfEntity> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val displayName = queryDisplayName(uri) ?: "Document_${System.currentTimeMillis()}.pdf"
            val cleanTitle = displayName.removeSuffix(".pdf").removeSuffix(".PDF")

            val pdfDir = File(context.filesDir, "user_pdfs").apply { if (!exists()) mkdirs() }
            val pdfId = "pdf_${section.lowercase()}_${System.currentTimeMillis()}"
            val destFile = File(pdfDir, "$pdfId.pdf")

            resolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Unable to read selected PDF file."))

            var pageCount = 1
            ParcelFileDescriptor.open(destFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    pageCount = renderer.pageCount.coerceAtLeast(1)
                }
            }

            val entity = UserPdfEntity(
                id = pdfId,
                section = section,
                title = cleanTitle,
                localFilePath = destFile.absolutePath,
                totalPages = pageCount,
                currentPage = 1,
                pagesReadToday = 0,
                totalPagesRead = 0,
                lastReadDateKey = "",
                bookmarkedPagesCsv = "",
                addedAtMillis = System.currentTimeMillis()
            )
            userPdfDao.upsertPdf(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePdfProgress(pdfId: String, page: Int, toggleBookmark: Boolean = false) {
        val current = userPdfDao.getById(pdfId) ?: return
        val today = todayDateKey()
        val safePage = page.coerceIn(1, current.totalPages.coerceAtLeast(1))
        val bookmarks = current.bookmarkedPagesCsv
            .split(",")
            .mapNotNull { it.toIntOrNull() }
            .toMutableSet()

        if (toggleBookmark) {
            if (bookmarks.contains(safePage)) bookmarks.remove(safePage) else bookmarks.add(safePage)
        }

        val isSameDay = current.lastReadDateKey == today
        val pageAdvanced = safePage != current.currentPage
        val newPagesToday = when {
            !isSameDay -> 1
            pageAdvanced -> current.pagesReadToday + 1
            else -> current.pagesReadToday.coerceAtLeast(1)
        }
        val newTotal = if (pageAdvanced || current.totalPagesRead == 0) {
            current.totalPagesRead + 1
        } else {
            current.totalPagesRead
        }

        userPdfDao.upsertPdf(
            current.copy(
                currentPage = safePage,
                pagesReadToday = newPagesToday,
                totalPagesRead = newTotal,
                lastReadDateKey = today,
                bookmarkedPagesCsv = bookmarks.sorted().joinToString(",")
            )
        )
    }

    suspend fun deleteUserPdf(pdfId: String) = withContext(Dispatchers.IO) {
        val existing = userPdfDao.getById(pdfId)
        if (existing != null) {
            runCatching { File(existing.localFilePath).delete() }
            userPdfDao.deletePdf(pdfId)
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveHistoryResult(lessonId: String, score: Int, total: Int) {
        historyDao.upsert(
            HistoryProgressEntity(
                lessonId = lessonId,
                completed = true,
                quizScore = score,
                totalQuestions = total,
                completedDateKey = todayDateKey()
            )
        )
    }

    suspend fun saveEnglishSession(
        level: String,
        totalQuestions: Int,
        correctAnswers: Int,
        scoreOutOf10: Int,
        grammarTopic: String
    ) {
        englishDao.upsertSession(
            EnglishSessionEntity(
                dateKey = todayDateKey(),
                level = level,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                scoreOutOf10 = scoreOutOf10,
                grammarTopic = grammarTopic,
                completed = true
            )
        )
    }

    suspend fun refreshLiveNews(): Result<Int> {
        val result = newsService.fetchLiveNews()
        return result.fold(
            onSuccess = { fetched ->
                val bookmarkedIds = newsDao.getBookmarkedOnce().map { it.id }.toSet()
                val merged = fetched.map { item ->
                    if (item.id in bookmarkedIds) item.copy(bookmarked = true) else item
                }
                newsDao.clearNonBookmarkedCache()
                newsDao.insertAll(merged)
                Result.success(merged.size)
            },
            onFailure = { error -> Result.failure(error) }
        )
    }

    suspend fun toggleNewsBookmark(id: String, bookmarked: Boolean) {
        newsDao.setBookmarked(id, bookmarked)
    }

    suspend fun clearNewsCache() {
        newsDao.clearNonBookmarkedCache()
    }
}
