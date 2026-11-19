package com.example.data.content

data class TranslationPrompt(
    val id: String,
    val sourceText: String,
    val expectedTranslation: String,
    val grammarHintBn: String
)

data class GrammarQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationBn: String
)

data class SentenceBuildItem(
    val id: String,
    val promptBn: String,
    val correctSentence: String,
    val shuffledWords: List<String>,
    val explanationBn: String
)

data class DailyEnglishLesson(
    val level: String,
    val title: String,
    val grammarTopic: String,
    val grammarExplanationBn: String,
    val enToBn: List<TranslationPrompt>,
    val bnToEn: List<TranslationPrompt>,
    val grammarQuestions: List<GrammarQuestion>,
    val sentenceBuildItems: List<SentenceBuildItem>,
    val dailyChallenge: List<GrammarQuestion>
)

object EnglishContentBank {

    val levels = listOf("Beginner", "Elementary", "Intermediate", "Advanced")

    fun getLessonForLevel(level: String): DailyEnglishLesson {
        return when (level) {
            "Beginner" -> beginnerLesson
            "Elementary" -> elementaryLesson
            "Advanced" -> advancedLesson
            else -> intermediateLesson
        }
    }

    private val beginnerLesson = DailyEnglishLesson(
        level = "Beginner",
        title = "Daily Routine & Present Simple",
        grammarTopic = "Present Simple Tense (সাধারণ বর্তমান কাল)",
        grammarExplanationBn = "প্রতিদিনের অভ্যাস বা চিরন্তন সত্য বোঝাতে Present Simple Tense ব্যবহৃত হয়। Subject যদি He, She বা It (Third Person Singular) হয়, তবে মূল verb-এর সাথে s বা es যোগ হয়। যেমন: I read -> He reads.",
        enToBn = listOf(
            TranslationPrompt(
                id = "b_e2b_1",
                sourceText = "I wake up early every morning.",
                expectedTranslation = "আমি প্রতিদিন সকালে তাড়াতাড়ি ঘুম থেকে উঠি।",
                grammarHintBn = "Wake up = ঘুম থেকে ওঠা; every morning = প্রতিদিন সকালে (নিয়মিত অভ্যাস)।"
            ),
            TranslationPrompt(
                id = "b_e2b_2",
                sourceText = "She reads a book before sleeping.",
                expectedTranslation = "সে ঘুমানোর আগে একটি বই পড়ে।",
                grammarHintBn = "She (Third person singular) হওয়ায় read-এর সাথে 's' বসেছে (reads)।"
            )
        ),
        bnToEn = listOf(
            TranslationPrompt(
                id = "b_b2e_1",
                sourceText = "আমি প্রতিদিন ইংরেজি অনুশীলন করি।",
                expectedTranslation = "I practice English every day.",
                grammarHintBn = "Subject (I) + Verb (practice) + Object (English) + Time (every day)।"
            ),
            TranslationPrompt(
                id = "b_b2e_2",
                sourceText = "তারা মসজিদে যায়।",
                expectedTranslation = "They go to the mosque.",
                grammarHintBn = "They plural হওয়ায় verb-এর সাথে s/es যোগ হবে না।"
            )
        ),
        grammarQuestions = listOf(
            GrammarQuestion(
                id = "b_g_1",
                question = "He ___ five pages of Bengali literature every night.",
                options = listOf("read", "reads", "reading", "is read"),
                correctIndex = 1,
                explanationBn = "He হলো third person singular, তাই Present Simple-এ 'reads' সঠিক।"
            ),
            GrammarQuestion(
                id = "b_g_2",
                question = "We ___ to learn ten new words today.",
                options = listOf("wants", "want", "wanting", "wanted to"),
                correctIndex = 1,
                explanationBn = "We (plural subject)-এর পর verb-এর base form 'want' বসে।"
            )
        ),
        sentenceBuildItems = listOf(
            SentenceBuildItem(
                id = "b_sb_1",
                promptBn = "আমি প্রতিদিন সকালে চা পান করি।",
                correctSentence = "I drink tea every morning",
                shuffledWords = listOf("morning", "I", "tea", "every", "drink"),
                explanationBn = "ইংরেজি বাক্যের স্বাভাবিক গঠন: Subject (I) + Verb (drink) + Object (tea) + Time (every morning)।"
            )
        ),
        dailyChallenge = listOf(
            GrammarQuestion(
                id = "b_dc_1",
                question = "Choose the correct negative sentence:",
                options = listOf("He don't like coffee.", "He doesn't like coffee.", "He not like coffee.", "He doesn't likes coffee."),
                correctIndex = 1,
                explanationBn = "Third person singular-এ negative করতে 'doesn't + base verb' বসে।"
            ),
            GrammarQuestion(
                id = "b_dc_2",
                question = "What is the meaning of 'consistent'?",
                options = listOf("অনিয়মিত", "ধারাবাহিক", "ক্লান্ত", "দ্রুত"),
                correctIndex = 1,
                explanationBn = "Consistent অর্থ ধারাবাহিক বা নিয়মিত।"
            ),
            GrammarQuestion(
                id = "b_dc_3",
                question = "___ you read the newspaper today?",
                options = listOf("Does", "Did", "Is", "Are"),
                correctIndex = 1,
                explanationBn = "অতীতে সম্পন্ন প্রশ্ন করতে 'Did + subject + base verb' ব্যবহৃত হয়।"
            )
        )
    )

    private val elementaryLesson = DailyEnglishLesson(
        level = "Elementary",
        title = "Past Experiences & Connectors",
        grammarTopic = "Past Simple vs Past Continuous",
        grammarExplanationBn = "অতীতে কোনো কাজ নির্দিষ্ট সময়ে শেষ হলে Past Simple (V2) এবং অতীতে কোনো কাজ চলছিল বোঝাতে Past Continuous (was/were + V-ing) বসে। যেমন: While I was reading, the rain started.",
        enToBn = listOf(
            TranslationPrompt(
                id = "e_e2b_1",
                sourceText = "I was reading a story when the call to prayer began.",
                expectedTranslation = "আযান শুরু হওয়ার সময় আমি একটি গল্প পড়ছিলাম।",
                grammarHintBn = "Was reading = পড়ছিলাম (চলমান অতীত); began = শুরু হলো (সাধারণ অতীত)।"
            )
        ),
        bnToEn = listOf(
            TranslationPrompt(
                id = "e_b2e_1",
                sourceText = "গতকাল আমি দশটি নতুন শব্দ শিখেছি।",
                expectedTranslation = "Yesterday I learned ten new words.",
                grammarHintBn = "Yesterday থাকায় verb-এর Past form (learned) ব্যবহৃত হয়।"
            )
        ),
        grammarQuestions = listOf(
            GrammarQuestion(
                id = "e_g_1",
                question = "They ___ walking to the library when it started to rain.",
                options = listOf("was", "were", "are", "have"),
                correctIndex = 1,
                explanationBn = "They plural হওয়ায় Past Continuous-এ 'were' বসে।"
            ),
            GrammarQuestion(
                id = "e_g_2",
                question = "She ___ her homework before Maghrib.",
                options = listOf("finish", "finished", "finishing", "finishes yesterday"),
                correctIndex = 1,
                explanationBn = "অতীতে শেষ হওয়া কাজ বোঝাতে 'finished' সঠিক।"
            )
        ),
        sentenceBuildItems = listOf(
            SentenceBuildItem(
                id = "e_sb_1",
                promptBn = "গত সন্ধ্যায় আমরা বিশ্ব সংবাদ পড়েছিলাম।",
                correctSentence = "We read world news last evening",
                shuffledWords = listOf("evening", "news", "We", "last", "read", "world"),
                explanationBn = "Subject (We) + Verb (read) + Object (world news) + Time (last evening)।"
            )
        ),
        dailyChallenge = listOf(
            GrammarQuestion(
                id = "e_dc_1",
                question = "Which connector shows contrast?",
                options = listOf("Because", "Although", "Therefore", "And"),
                correctIndex = 1,
                explanationBn = "Although (যদিও) বিপরীত ভাব প্রকাশ করে।"
            ),
            GrammarQuestion(
                id = "e_dc_2",
                question = "I have lived in Dhaka ___ 2020.",
                options = listOf("for", "since", "from", "at"),
                correctIndex = 1,
                explanationBn = "নির্দিষ্ট শুরুর সাল বা সময় বোঝাতে 'since' বসে।"
            ),
            GrammarQuestion(
                id = "e_dc_3",
                question = "He speaks English very ___.",
                options = listOf("fluent", "fluently", "fluency", "more fluent"),
                correctIndex = 1,
                explanationBn = "Verb (speaks)-কে modify করতে adverb 'fluently' বসে।"
            )
        )
    )

    private val intermediateLesson = DailyEnglishLesson(
        level = "Intermediate",
        title = "Clarity, Present Perfect & Habits",
        grammarTopic = "Present Perfect (have/has + V3)",
        grammarExplanationBn = "কোনো কাজ অতীতে শুরু হয়ে এখনও তার ফল বর্তমান থাকলে অথবা সদ্য শেষ হলে Present Perfect Tense (have/has + Past Participle) ব্যবহৃত হয়। যেমন: I have completed my daily reading.",
        enToBn = listOf(
            TranslationPrompt(
                id = "i_e2b_1",
                sourceText = "Consistent daily habits gradually transform a person's character.",
                expectedTranslation = "ধারাবাহিক দৈনন্দিন অভ্যাস ধীরে ধীরে একজন মানুষের চরিত্রকে বদলে দেয়।",
                grammarHintBn = "Consistent = ধারাবাহিক, gradually = ধীরে ধীরে, transform = পরিবর্তন করা।"
            ),
            TranslationPrompt(
                id = "i_e2b_2",
                sourceText = "I have already finished today's vocabulary revision.",
                expectedTranslation = "আমি ইতিমধ্যেই আজকের শব্দভান্ডার পুনরাবৃত্তি শেষ করেছি।",
                grammarHintBn = "Have + already + finished (V3) দ্বারা কাজটি সদ্য সম্পন্ন হয়েছে বোঝায়।"
            )
        ),
        bnToEn = listOf(
            TranslationPrompt(
                id = "i_b2e_1",
                sourceText = "নিয়মিত বই পড়া আমাদের চিন্তাশক্তিকে প্রসারিত করে।",
                expectedTranslation = "Reading books regularly broadens our thinking.",
                grammarHintBn = "Gerund 'Reading books' এখানে subject হিসেবে কাজ করছে, তাই verb 'broadens' singular হবে।"
            ),
            TranslationPrompt(
                id = "i_b2e_2",
                sourceText = "যদি আমরা সময়ের সঠিক ব্যবহার করি, তবে আমরা সফল হব।",
                expectedTranslation = "If we use time properly, we will succeed.",
                grammarHintBn = "First Conditional: If + Present Simple, Subject + will + base verb."
            )
        ),
        grammarQuestions = listOf(
            GrammarQuestion(
                id = "i_g_1",
                question = "She ___ three chapters of Tagore's short stories this week.",
                options = listOf("has read", "have read", "is read", "reading"),
                correctIndex = 0,
                explanationBn = "She (singular) + Present Perfect হওয়ায় 'has read' সঠিক।"
            ),
            GrammarQuestion(
                id = "i_g_2",
                question = "If you practice every morning, your fluency ___ noticeably.",
                options = listOf("improved", "will improve", "improving", "was improve"),
                correctIndex = 1,
                explanationBn = "First Conditional-এ main clause-এ 'will + base verb' বসে।"
            )
        ),
        sentenceBuildItems = listOf(
            SentenceBuildItem(
                id = "i_sb_1",
                promptBn = "শৃঙ্খলা আমাদের প্রতিদিন আরও ভালো মানুষ হতে সাহায্য করে।",
                correctSentence = "Discipline helps us grow better every day",
                shuffledWords = listOf("better", "Discipline", "day", "us", "grow", "helps", "every"),
                explanationBn = "Structure: Subject (Discipline) + Verb (helps) + Object (us) + Bare Infinitive (grow better) + Time (every day)।"
            )
        ),
        dailyChallenge = listOf(
            GrammarQuestion(
                id = "i_dc_1",
                question = "Neither the teacher nor the students ___ late today.",
                options = listOf("was", "were", "is", "has been"),
                correctIndex = 1,
                explanationBn = "Neither...nor-এ নিকটবর্তী subject (students - plural) অনুযায়ী verb 'were' বসে।"
            ),
            GrammarQuestion(
                id = "i_dc_2",
                question = "I look forward to ___ from you soon.",
                options = listOf("hear", "hearing", "heard", "be hear"),
                correctIndex = 1,
                explanationBn = "'Look forward to'-এর পর verb-এর সাথে 'ing' (gerund) বসে।"
            ),
            GrammarQuestion(
                id = "i_dc_3",
                question = "Choose the closest synonym for 'perseverance':",
                options = listOf("Hesitation", "Steadfastness", "Distraction", "Neglect"),
                correctIndex = 1,
                explanationBn = "Perseverance ও Steadfastness উভয়ের অর্থ অধ্যবসায় বা দৃঢ়তা।"
            )
        )
    )

    private val advancedLesson = DailyEnglishLesson(
        level = "Advanced",
        title = "Nuance, Inversion & Academic Expression",
        grammarTopic = "Negative Inversion (Not only... but also / Rarely)",
        grammarExplanationBn = "বাক্যে জোর দেওয়ার জন্য Not only, Rarely বা Seldom বাক্যের শুরুতে বসলে auxiliary verb subject-এর আগে আসে। যেমন: Not only does reading expand knowledge, but it also cultivates empathy.",
        enToBn = listOf(
            TranslationPrompt(
                id = "a_e2b_1",
                sourceText = "Not only did the scholars of Baghdad preserve ancient manuscripts, but they also pioneered experimental science.",
                expectedTranslation = "বাগদাদের পণ্ডিতরা কেবল প্রাচীন পাণ্ডুলিপি সংরক্ষণই করেননি, বরং তারা পরীক্ষামূলক বিজ্ঞানেরও পথপ্রদর্শক ছিলেন।",
                grammarHintBn = "Not only + did + subject + base verb (Inversion structure) জোরালো বক্তব্য তৈরিতে ব্যবহৃত হয়।"
            )
        ),
        bnToEn = listOf(
            TranslationPrompt(
                id = "a_b2e_1",
                sourceText = "দৈনিক সামান্য প্রচেষ্টাও দীর্ঘমেয়াদে অসাধারণ ফল বয়ে আনে।",
                expectedTranslation = "Even modest daily efforts yield remarkable results over the long term.",
                grammarHintBn = "Yield (ফল বয়ে আনা) ও modest (পরিমিত/সামান্য) প্রাঞ্জল উচ্চতর ইংরেজি শব্দ।"
            )
        ),
        grammarQuestions = listOf(
            GrammarQuestion(
                id = "a_g_1",
                question = "Rarely ___ such remarkable intellectual curiosity among young learners.",
                options = listOf("we witness", "do we witness", "we are witnessing", "witnessed we"),
                correctIndex = 1,
                explanationBn = "Rarely বাক্যের শুরুতে বসায় Inversion হয়: Rarely + do + subject + verb।"
            ),
            GrammarQuestion(
                id = "a_g_2",
                question = "Had I known about the lecture earlier, I ___ attended it.",
                options = listOf("will have", "would have", "had", "would"),
                correctIndex = 1,
                explanationBn = "Inverted Third Conditional: Had + S + V3, S + would have + V3।"
            )
        ),
        sentenceBuildItems = listOf(
            SentenceBuildItem(
                id = "a_sb_1",
                promptBn = "ইতিহাস অধ্যয়ন আমাদের বর্তমানকে আরও গভীরভাবে বুঝতে সাহায্য করে।",
                correctSentence = "Studying history enables us to interpret the present deeply",
                shuffledWords = listOf("interpret", "Studying", "deeply", "us", "present", "history", "to", "the", "enables"),
                explanationBn = "Gerund phrase (Studying history) + enables us to + base verb (interpret) + object + adverb।"
            )
        ),
        dailyChallenge = listOf(
            GrammarQuestion(
                id = "a_dc_1",
                question = "Which word best fits: 'The historian offered a ___ analysis free of bias.'",
                options = listOf("lucid", "hasty", "opaque", "arbitrary"),
                correctIndex = 0,
                explanationBn = "Lucid অর্থ স্বচ্ছ, স্পষ্ট ও সহজবোধ্য।"
            ),
            GrammarQuestion(
                id = "a_dc_2",
                question = "It is imperative that every student ___ on time.",
                options = listOf("arrives", "arrive", "arrived", "arriving"),
                correctIndex = 1,
                explanationBn = "Subjunctive mood ('It is imperative that...')-এ সর্বদা verb-এর base form বসে।"
            ),
            GrammarQuestion(
                id = "a_dc_3",
                question = "Identify the meaning of 'ephemeral':",
                options = listOf("চিরস্থায়ী", "ক্ষণস্থায়ী", "গভীর", "অনিবার্য"),
                correctIndex = 1,
                explanationBn = "Ephemeral অর্থ ক্ষণস্থায়ী বা স্বল্পকাল স্থায়ী।"
            )
        )
    )
}
