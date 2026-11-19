package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)
}

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE dateKey = :dateKey ORDER BY isCustom ASC, taskKey ASC")
    fun observeTasksForDate(dateKey: String): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks ORDER BY dateKey DESC")
    fun observeAllTasks(): Flow<List<DailyTaskEntity>>

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE dateKey = :dateKey")
    suspend fun countTasksForDate(dateKey: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: DailyTaskEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(tasks: List<DailyTaskEntity>)

    @Query("UPDATE daily_tasks SET isCompleted = :completed WHERE compositeId = :compositeId")
    suspend fun setTaskCompleted(compositeId: String, completed: Boolean)

    @Query("DELETE FROM daily_tasks WHERE compositeId = :compositeId")
    suspend fun deleteTask(compositeId: String)
}

@Dao
interface PrayerAlarmDao {
    @Query("SELECT * FROM prayer_alarms")
    fun observeAlarms(): Flow<List<PrayerAlarmEntity>>

    @Query("SELECT * FROM prayer_alarms")
    suspend fun getAlarmsOnce(): List<PrayerAlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlarm(alarm: PrayerAlarmEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(alarms: List<PrayerAlarmEntity>)
}

@Dao
interface VocabProgressDao {
    @Query("SELECT * FROM vocab_progress")
    fun observeAll(): Flow<List<VocabProgressEntity>>

    @Query("SELECT * FROM vocab_progress WHERE wordId = :wordId")
    suspend fun getByWordId(wordId: String): VocabProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: VocabProgressEntity)
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress")
    fun observeAll(): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    suspend fun getByBookId(bookId: String): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ReadingProgressEntity)
}

@Dao
interface HistoryProgressDao {
    @Query("SELECT * FROM history_progress")
    fun observeAll(): Flow<List<HistoryProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: HistoryProgressEntity)
}

@Dao
interface CachedNewsDao {
    @Query("SELECT * FROM cached_news ORDER BY cachedAtMillis DESC")
    fun observeAllNews(): Flow<List<CachedNewsEntity>>

    @Query("SELECT * FROM cached_news WHERE bookmarked = 1")
    suspend fun getBookmarkedOnce(): List<CachedNewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedNewsEntity>)

    @Query("UPDATE cached_news SET bookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, bookmarked: Boolean)

    @Query("SELECT MAX(cachedAtMillis) FROM cached_news")
    suspend fun getLatestCachedAt(): Long?

    @Query("DELETE FROM cached_news WHERE bookmarked = 0")
    suspend fun clearNonBookmarkedCache()
}

@Dao
interface EnglishSessionDao {
    @Query("SELECT * FROM english_sessions ORDER BY dateKey DESC")
    fun observeAllSessions(): Flow<List<EnglishSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: EnglishSessionEntity)
}

@Dao
interface UserPdfDao {
    @Query("SELECT * FROM user_pdfs ORDER BY addedAtMillis DESC")
    fun observeAllPdfs(): Flow<List<UserPdfEntity>>

    @Query("SELECT * FROM user_pdfs WHERE id = :id")
    suspend fun getById(id: String): UserPdfEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPdf(pdf: UserPdfEntity)

    @Query("DELETE FROM user_pdfs WHERE id = :id")
    suspend fun deletePdf(id: String)
}
