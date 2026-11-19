package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "FirebaseAiRepository"
private const val DEFAULT_GEMINI_MODEL = "gemini-3.8-flash"

data class TranslationEvaluation(
    val userAnswer: String,
    val correctAnswer: String,
    val mistakesFound: List<String>,
    val grammarExplanationBn: String,
    val alternatives: List<String>,
    val scoreOutOf10: Int,
    val encouragement: String,
    val isPlaceholderResponse: Boolean = false
)

data class ChatTurn(
    val isUser: Boolean,
    val text: String
)

interface AiRepository {
    val isLiveGeminiConnected: Boolean

    suspend fun evaluateTranslation(
        sourceText: String,
        expectedTranslation: String,
        userAnswer: String,
        direction: String,
        grammarHintBn: String
    ): TranslationEvaluation

    suspend fun summarizeNewsArticle(
        headline: String,
        summary: String,
        source: String
    ): String

    fun streamTeacherReply(
        history: List<ChatTurn>,
        userQuestion: String,
        topic: String
    ): Flow<String>
}

class FirebaseAiRepository : AiRepository {
    override val isLiveGeminiConnected: Boolean = true

    private val teacherSystemInstruction = content {
        text(
            """
            You are DailyPath's friendly Personal AI Teacher for a Bangladeshi learner.
            - Respond in simple, clear, encouraging Bangla by default, with English examples whenever helpful (especially for English grammar, vocabulary, or translation).
            - Topics you teach: English grammar, vocabulary, translation, Bengali literature, Islamic history, and general knowledge.
            - Be concise, structured (use short bullet points where helpful), and warm.
            - Distinguish verified facts from uncertain information. NEVER invent Quranic verses, Hadith references, or historical dates.
            """.trimIndent()
        )
    }

    private val teacherModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_GEMINI_MODEL,
            systemInstruction = teacherSystemInstruction
        )
    }

    private val summaryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_GEMINI_MODEL,
            systemInstruction = content {
                text("Summarize only the provided real news headline and snippet concisely in 3 short bullet points (Bangla with key English terms). Never invent facts beyond the provided text.")
            }
        )
    }

    private val translationEvalSchema = Schema.obj(
        properties = mapOf(
            "correctAnswer" to Schema.string(description = "A natural, accurate target-language translation"),
            "mistakesFound" to Schema.array(
                items = Schema.string(),
                description = "Specific grammar, vocabulary, or meaning mistakes (or a positive note if none)"
            ),
            "grammarExplanationBn" to Schema.string(description = "Clear, simple Bangla explanation of the grammar and meaning"),
            "alternatives" to Schema.array(
                items = Schema.string(),
                description = "1 to 2 natural alternative translations"
            ),
            "scoreOutOf10" to Schema.integer(description = "Score from 0 to 10 evaluating real meaning and grammar"),
            "encouragement" to Schema.string(description = "Short encouraging feedback in Bangla")
        )
    )

    private val translationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_GEMINI_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationEvalSchema
            }
        )
    }

    override suspend fun evaluateTranslation(
        sourceText: String,
        expectedTranslation: String,
        userAnswer: String,
        direction: String,
        grammarHintBn: String
    ): TranslationEvaluation {
        val cleanUser = userAnswer.trim()
        if (cleanUser.isBlank()) {
            return TranslationEvaluation(
                userAnswer = "(Empty)",
                correctAnswer = expectedTranslation,
                mistakesFound = listOf("কোনো উত্তর লেখা হয়নি।"),
                grammarExplanationBn = grammarHintBn,
                alternatives = listOf(expectedTranslation),
                scoreOutOf10 = 0,
                encouragement = "একটি উত্তর লিখে আবার চেষ্টা করুন!",
                isPlaceholderResponse = false
            )
        }

        val prompt = """
            Evaluate a student's translation ($direction).
            Source sentence: "$sourceText"
            Reference translation: "$expectedTranslation"
            Student's answer: "$cleanUser"
            Grammar hint: "$grammarHintBn"

            Important rules:
            - Do NOT mark a grammatically correct, accurate alternative translation as wrong just because it differs from the reference wording.
            - Evaluate actual meaning, grammar, vocabulary, and natural phrasing.
            - Explain mistakes and grammar in simple, friendly Bangla.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                translationModel.generateContent(prompt)
            }
            val jsonString = response.text
                ?: throw IllegalStateException("Gemini returned an empty response.")
            val json = JSONObject(jsonString)

            val mistakesArr = json.optJSONArray("mistakesFound")
            val mistakes = if (mistakesArr != null && mistakesArr.length() > 0) {
                (0 until mistakesArr.length()).map { mistakesArr.getString(it) }
            } else {
                listOf("বড় কোনো ভুল পাওয়া যায়নি।")
            }

            val altArr = json.optJSONArray("alternatives")
            val alternatives = if (altArr != null && altArr.length() > 0) {
                (0 until altArr.length()).map { altArr.getString(it) }
            } else {
                listOf(expectedTranslation)
            }

            TranslationEvaluation(
                userAnswer = cleanUser,
                correctAnswer = json.optString("correctAnswer", expectedTranslation),
                mistakesFound = mistakes,
                grammarExplanationBn = json.optString("grammarExplanationBn", grammarHintBn),
                alternatives = alternatives,
                scoreOutOf10 = json.optInt("scoreOutOf10", 7).coerceIn(0, 10),
                encouragement = json.optString("encouragement", "দারুণ চেষ্টা! প্রতিদিন চর্চা চালিয়ে যান।"),
                isPlaceholderResponse = false
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Translation evaluation timed out", e)
            throw IllegalStateException("AI evaluation timed out after 60 seconds. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Translation evaluation failed", e)
            throw e
        }
    }

    override suspend fun summarizeNewsArticle(
        headline: String,
        summary: String,
        source: String
    ): String {
        val prompt = """
            Source: $source
            Headline: $headline
            Snippet: $summary

            Provide a concise, factual summary in simple Bangla (with key English terms if needed) based ONLY on the above text.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                summaryModel.generateContent(prompt)
            }
            response.text?.trim()?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Gemini returned an empty summary.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "News summarization timed out", e)
            throw IllegalStateException("Summarization timed out. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "News summarization failed", e)
            throw e
        }
    }

    override fun streamTeacherReply(
        history: List<ChatTurn>,
        userQuestion: String,
        topic: String
    ): Flow<String> = flow {
        try {
            val sdkHistory = history.takeLast(14).map { turn ->
                content(role = if (turn.isUser) "user" else "model") {
                    text(turn.text)
                }
            }
            val chat = teacherModel.startChat(history = sdkHistory)
            val taggedPrompt = "[Topic: $topic]\n$userQuestion"

            val accumulated = StringBuilder()
            withTimeout(60_000L) {
                chat.sendMessageStream(taggedPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Gemini returned an empty response. Please try again.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Teacher chat streaming timed out", e)
            throw IllegalStateException("Response timed out after 60 seconds. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Teacher chat streaming failed", e)
            throw e
        }
    }
}
