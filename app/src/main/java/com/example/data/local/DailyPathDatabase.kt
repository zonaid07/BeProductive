package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        DailyTaskEntity::class,
        PrayerAlarmEntity::class,
        VocabProgressEntity::class,
        ReadingProgressEntity::class,
        UserPdfEntity::class,
        HistoryProgressEntity::class,
        CachedNewsEntity::class,
        EnglishSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DailyPathDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun prayerAlarmDao(): PrayerAlarmDao
    abstract fun vocabProgressDao(): VocabProgressDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun userPdfDao(): UserPdfDao
    abstract fun historyProgressDao(): HistoryProgressDao
    abstract fun cachedNewsDao(): CachedNewsDao
    abstract fun englishSessionDao(): EnglishSessionDao

    companion object {
        @Volatile
        private var INSTANCE: DailyPathDatabase? = null

        fun getInstance(context: Context): DailyPathDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DailyPathDatabase::class.java,
                    "dailypath_db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
