package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Learner",
    val preferredLanguage: String = "EN", // "EN" or "BN"
    val englishLevel: String = "Intermediate", // Beginner, Elementary, Intermediate, Advanced
    val cityName: String = "Dhaka, Bangladesh",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val calculationMethod: String = "KARACHI", // MWL, ISNA, UMM_AL_QURA, KARACHI, EGYPT
    val asrMethod: String = "HANAFI", // STANDARD, HANAFI
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "07:00",
    val tahajjudEnabled: Boolean = false,
    val tahajjudTime: String = "03:45",
    val readerFontSizeSp: Float = 18f,
    val readerLineHeightMultiplier: Float = 1.6f,
    val readerDarkMode: Boolean = false
)

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey val compositeId: String, // "${dateKey}_${taskKey}"
    val dateKey: String,
    val taskKey: String,
    val titleEn: String,
    val titleBn: String,
    val category: String,
    val isCompleted: Boolean = false,
    val isCustom: Boolean = false,
    val reminderTime: String = ""
)

@Entity(tableName = "prayer_alarms")
data class PrayerAlarmEntity(
    @PrimaryKey val prayerName: String, // Fajr, Dhuhr, Asr, Maghrib, Isha, Tahajjud
    val enabled: Boolean = true,
    val useCustomTime: Boolean = false,
    val customTime24: String = "", // e.g. "05:15" when useCustomTime is true
    val reminderMinutesBefore: Int = 10, // 0, 5, 10, 15, 30 or custom
    val soundMode: String = "NOTIFICATION", // ADHAN, NOTIFICATION, CUSTOM
    val customSoundName: String = "Default Chime",
    val customSoundUri: String = "",
    val customLabel: String = "",
    val repeatDaysCsv: String = "1,2,3,4,5,6,7", // 1=Sun .. 7=Sat
    val vibrationEnabled: Boolean = true,
    val volumePercent: Int = 80
)

@Entity(tableName = "vocab_progress")
data class VocabProgressEntity(
    @PrimaryKey val wordId: String,
    val learned: Boolean = false,
    val bookmarked: Boolean = false,
    val customCollection: String = "",
    val mistakesCount: Int = 0,
    val reviewsCount: Int = 0,
    val nextReviewDayEpoch: Long = 0L,
    val lastPracticedDateKey: String = ""
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val bookId: String,
    val currentPage: Int = 1,
    val pagesReadToday: Int = 0,
    val totalPagesRead: Int = 0,
    val lastReadDateKey: String = "",
    val bookmarkedPagesCsv: String = ""
)

@Entity(tableName = "user_pdfs")
data class UserPdfEntity(
    @PrimaryKey val id: String,
    val section: String, // "READING" or "HISTORY"
    val title: String,
    val localFilePath: String,
    val totalPages: Int = 1,
    val currentPage: Int = 1,
    val pagesReadToday: Int = 0,
    val totalPagesRead: Int = 0,
    val lastReadDateKey: String = "",
    val bookmarkedPagesCsv: String = "",
    val addedAtMillis: Long = 0L
)

@Entity(tableName = "history_progress")
data class HistoryProgressEntity(
    @PrimaryKey val lessonId: String,
    val completed: Boolean = false,
    val quizScore: Int = 0,
    val totalQuestions: Int = 5,
    val completedDateKey: String = ""
)

@Entity(tableName = "cached_news")
data class CachedNewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val url: String,
    val imageUrl: String,
    val publishedAt: String,
    val category: String,
    val region: String, // BANGLADESH or INTERNATIONAL
    val bookmarked: Boolean = false,
    val cachedAtMillis: Long = 0L
)

@Entity(tableName = "english_sessions")
data class EnglishSessionEntity(
    @PrimaryKey val dateKey: String,
    val level: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scoreOutOf10: Int,
    val grammarTopic: String,
    val completed: Boolean
)
