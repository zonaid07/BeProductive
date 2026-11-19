package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatTurn
import com.example.data.DailyPathRepository
import com.example.data.TranslationEvaluation
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
import com.example.prayer.AsrMethod
import com.example.prayer.CalculationMethod
import com.example.prayer.CityOption
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.PrayerTimeCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val topic: String
)

class DailyPathViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DailyPathRepository(
        context = application.applicationContext,
        db = DailyPathDatabase.getInstance(application.applicationContext)
    )

    val todayKey: String = repository.todayDateKey()
    val isLiveGeminiConnected: Boolean = repository.aiRepository.isLiveGeminiConnected

    val profile: StateFlow<UserProfileEntity> = repository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserProfileEntity())

    private val _selectedTaskDate = MutableStateFlow(todayKey)
    val selectedTaskDate: StateFlow<String> = _selectedTaskDate.asStateFlow()

    val todayTasks: StateFlow<List<DailyTaskEntity>> = repository.observeTasksForDate(todayKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allTasks: StateFlow<List<DailyTaskEntity>> = repository.observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val prayerAlarms: StateFlow<List<PrayerAlarmEntity>> = repository.observePrayerAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val vocabProgress: StateFlow<Map<String, VocabProgressEntity>> = repository.observeVocabProgress()
        .map { list -> list.associateBy { it.wordId } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val readingProgress: StateFlow<Map<String, ReadingProgressEntity>> = repository.observeReadingProgress()
        .map { list -> list.associateBy { it.bookId } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val userPdfs: StateFlow<List<UserPdfEntity>> = repository.observeUserPdfs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val historyProgress: StateFlow<Map<String, HistoryProgressEntity>> = repository.observeHistoryProgress()
        .map { list -> list.associateBy { it.lessonId } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val newsArticles: StateFlow<List<CachedNewsEntity>> = repository.observeNews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val englishSessions: StateFlow<List<EnglishSessionEntity>> = repository.observeEnglishSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _clockTick = MutableStateFlow(System.currentTimeMillis())

    val prayerSchedule: StateFlow<DailyPrayerSchedule> = combine(profile, _clockTick) { p, _ ->
        PrayerTimeCalculator.calculateSchedule(
            latitude = p.latitude,
            longitude = p.longitude,
            method = CalculationMethod.fromKey(p.calculationMethod),
            asrMethod = AsrMethod.fromKey(p.asrMethod),
            nowCalendar = Calendar.getInstance()
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            PrayerTimeCalculator.calculateSchedule(
                23.8103, 90.4125, CalculationMethod.KARACHI, AsrMethod.HANAFI
            )
        )

    private val _isOnline = MutableStateFlow(checkNetworkAvailable())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _newsLoading = MutableStateFlow(false)
    val newsLoading: StateFlow<Boolean> = _newsLoading.asStateFlow()

    private val _newsError = MutableStateFlow<String?>(null)
    val newsError: StateFlow<String?> = _newsError.asStateFlow()

    private val _newsSummaries = MutableStateFlow<Map<String, String>>(emptyMap())
    val newsSummaries: StateFlow<Map<String, String>> = _newsSummaries.asStateFlow()

    private val _aiMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "welcome",
                isUser = false,
                text = "আসসালামু আলাইকুম! আমি আপনার DailyPath ব্যক্তিগত শিক্ষা সহায়ক। ইংরেজি ব্যাকরণ, শব্দভান্ডার, অনুবাদ, বাংলা সাহিত্য বা ইসলামের ইতিহাস সম্পর্কে যেকোনো প্রশ্ন করুন।",
                topic = "General"
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    val cachedFormattedDate: String = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeededForToday()
            // Defer initial network fetch slightly so first frame renders immediately,
            // and skip if fresh news was already cached within the last 15 minutes.
            if (!repository.hasFreshNewsCache()) {
                delay(350L)
                refreshNews()
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                _clockTick.value = System.currentTimeMillis()
                _isOnline.value = checkNetworkAvailable()
            }
        }
    }

    fun checkNetworkAvailable(): Boolean {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isOnline.value = checkNetworkAvailable()
            if (!_isOnline.value) {
                _newsError.value = "Offline mode — showing previously cached news (may not be current)."
                return@launch
            }
            _newsLoading.value = true
            _newsError.value = null
            val result = repository.refreshLiveNews()
            result.onFailure { err ->
                _newsError.value = err.message ?: "Unable to fetch live news right now."
            }
            _newsLoading.value = false
        }
    }

    fun toggleNewsBookmark(id: String, bookmarked: Boolean) {
        viewModelScope.launch { repository.toggleNewsBookmark(id, bookmarked) }
    }

    fun summarizeNews(article: CachedNewsEntity) {
        viewModelScope.launch {
            _newsSummaries.value = _newsSummaries.value + (article.id to "Summarizing with Gemini...")
            try {
                val summary = repository.aiRepository.summarizeNewsArticle(
                    headline = article.title,
                    summary = article.summary,
                    source = article.source
                )
                _newsSummaries.value = _newsSummaries.value + (article.id to summary)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _newsSummaries.value = _newsSummaries.value + (
                    article.id to "Could not summarize: ${e.localizedMessage ?: "Network or AI error"}"
                )
            }
        }
    }

    fun clearNewsCache() {
        viewModelScope.launch { repository.clearNewsCache() }
    }

    fun toggleTask(compositeId: String, completed: Boolean) {
        viewModelScope.launch { repository.toggleTask(compositeId, completed) }
    }

    fun addCustomTask(dateKey: String, title: String, category: String, reminderTime: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.ensureDefaultTasksForDate(dateKey)
            repository.addCustomTask(dateKey, title, category, reminderTime)
        }
    }

    fun deleteTask(compositeId: String) {
        viewModelScope.launch { repository.deleteTask(compositeId) }
    }

    fun selectTaskDate(dateKey: String) {
        _selectedTaskDate.value = dateKey
        viewModelScope.launch { repository.ensureDefaultTasksForDate(dateKey) }
    }

    fun selectCity(city: CityOption) {
        viewModelScope.launch {
            repository.updateProfile {
                it.copy(
                    cityName = city.displayName,
                    latitude = city.latitude,
                    longitude = city.longitude
                )
            }
            _clockTick.value = System.currentTimeMillis()
        }
    }

    fun updateDetectedLocation(lat: Double, lon: Double, label: String) {
        viewModelScope.launch {
            repository.updateProfile {
                it.copy(cityName = label, latitude = lat, longitude = lon)
            }
            _clockTick.value = System.currentTimeMillis()
        }
    }

    fun updateCalculationMethod(methodKey: String) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(calculationMethod = methodKey) }
            _clockTick.value = System.currentTimeMillis()
        }
    }

    fun updateAsrMethod(asrKey: String) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(asrMethod = asrKey) }
            _clockTick.value = System.currentTimeMillis()
        }
    }

    fun updatePrayerAlarm(alarm: PrayerAlarmEntity) {
        viewModelScope.launch { repository.updatePrayerAlarm(alarm) }
    }

    fun testPrayerAlarm(alarm: PrayerAlarmEntity) {
        repository.testPrayerAlarm(alarm)
    }

    fun updateProfile(transform: (UserProfileEntity) -> UserProfileEntity) {
        viewModelScope.launch { repository.updateProfile(transform) }
    }

    suspend fun evaluateTranslation(
        sourceText: String,
        expectedTranslation: String,
        userAnswer: String,
        direction: String,
        grammarHintBn: String
    ): TranslationEvaluation {
        return repository.aiRepository.evaluateTranslation(
            sourceText = sourceText,
            expectedTranslation = expectedTranslation,
            userAnswer = userAnswer,
            direction = direction,
            grammarHintBn = grammarHintBn
        )
    }

    fun saveEnglishSession(
        level: String,
        totalQuestions: Int,
        correctAnswers: Int,
        scoreOutOf10: Int,
        grammarTopic: String
    ) {
        viewModelScope.launch {
            repository.saveEnglishSession(level, totalQuestions, correctAnswers, scoreOutOf10, grammarTopic)
        }
    }

    fun markVocabLearned(wordId: String, learned: Boolean) {
        val epochDay = System.currentTimeMillis() / 86_400_000L
        viewModelScope.launch {
            repository.updateVocabProgress(wordId) {
                it.copy(
                    learned = learned,
                    reviewsCount = it.reviewsCount + 1,
                    nextReviewDayEpoch = if (learned) epochDay + 3 else epochDay,
                    lastPracticedDateKey = todayKey
                )
            }
        }
    }

    fun toggleVocabBookmark(wordId: String) {
        viewModelScope.launch {
            repository.updateVocabProgress(wordId) { it.copy(bookmarked = !it.bookmarked) }
        }
    }

    fun recordVocabPracticeResult(wordId: String, wasCorrect: Boolean) {
        val epochDay = System.currentTimeMillis() / 86_400_000L
        viewModelScope.launch {
            repository.updateVocabProgress(wordId) {
                val newMistakes = if (wasCorrect) it.mistakesCount else it.mistakesCount + 1
                val nextDays = if (wasCorrect) (it.reviewsCount + 2).coerceAtMost(14) else 0
                it.copy(
                    learned = wasCorrect || it.learned,
                    mistakesCount = newMistakes,
                    reviewsCount = it.reviewsCount + 1,
                    nextReviewDayEpoch = epochDay + nextDays,
                    lastPracticedDateKey = todayKey
                )
            }
        }
    }

    fun assignVocabCollection(wordId: String, collectionName: String) {
        viewModelScope.launch {
            repository.updateVocabProgress(wordId) { it.copy(customCollection = collectionName.trim()) }
        }
    }

    fun updateReadingPage(bookId: String, page: Int, toggleBookmark: Boolean = false) {
        viewModelScope.launch {
            repository.updateReadingProgress(bookId, page, toggleBookmark)
        }
    }

    fun importUserPdf(uri: Uri, section: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.importUserPdf(uri, section)
            res.onSuccess { pdf ->
                onResult("Uploaded '${pdf.title}' (${pdf.totalPages} pages)")
            }.onFailure { err ->
                onResult(err.localizedMessage ?: "Failed to import PDF")
            }
        }
    }

    fun updatePdfPage(pdfId: String, page: Int, toggleBookmark: Boolean = false) {
        viewModelScope.launch {
            repository.updatePdfProgress(pdfId, page, toggleBookmark)
        }
    }

    fun deleteUserPdf(pdfId: String) {
        viewModelScope.launch {
            repository.deleteUserPdf(pdfId)
        }
    }

    fun saveHistoryQuizResult(lessonId: String, score: Int, total: Int) {
        viewModelScope.launch {
            repository.saveHistoryResult(lessonId, score, total)
        }
    }

    fun clearAiChat() {
        _aiError.value = null
        _aiMessages.value = listOf(
            AiChatMessage(
                id = "welcome_${System.currentTimeMillis()}",
                isUser = false,
                text = "আসসালামু আলাইকুম! আমি আপনার DailyPath ব্যক্তিগত শিক্ষা সহায়ক (Powered by Gemini)। ইংরেজি ব্যাকরণ, শব্দভান্ডার, অনুবাদ, বাংলা সাহিত্য বা ইসলামের ইতিহাস সম্পর্কে যেকোনো প্রশ্ন করুন।",
                topic = "General"
            )
        )
    }

    fun askAiAssistant(question: String, topic: String) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isBlank() || _aiLoading.value) return

        val historyBeforeTurn = _aiMessages.value
            .filterNot { it.id.startsWith("welcome") }
            .map { ChatTurn(isUser = it.isUser, text = it.text) }

        val userMsg = AiChatMessage("u_${System.currentTimeMillis()}", true, cleanQuestion, topic)
        val replyId = "a_${System.currentTimeMillis() + 1}"
        _aiError.value = null
        _aiMessages.value = _aiMessages.value + userMsg + AiChatMessage(replyId, false, "", topic)

        viewModelScope.launch {
            _aiLoading.value = true
            try {
                repository.aiRepository.streamTeacherReply(
                    history = historyBeforeTurn,
                    userQuestion = cleanQuestion,
                    topic = topic
                ).collect { streamedText ->
                    _aiMessages.value = _aiMessages.value.map { msg ->
                        if (msg.id == replyId) msg.copy(text = streamedText) else msg
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                // Remove empty placeholder bubble on error and surface real error message
                _aiMessages.value = _aiMessages.value.filterNot { it.id == replyId && it.text.isBlank() }
                _aiError.value = e.localizedMessage ?: "Failed to get response from Gemini. Please check your internet connection and try again."
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun greetingText(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isBn = profile.value.preferredLanguage == "BN"
        return when {
            hour < 12 -> if (isBn) "সুপ্রভাত" else "Good Morning"
            hour < 17 -> if (isBn) "শুভ অপরাহ্ণ" else "Good Afternoon"
            hour < 21 -> if (isBn) "শুভ সন্ধ্যা" else "Good Evening"
            else -> if (isBn) "শুভ রাত্রি" else "Peaceful Night"
        }
    }

    fun formattedDate(): String = cachedFormattedDate
}
