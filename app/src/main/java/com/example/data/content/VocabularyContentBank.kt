package com.example.data.content

data class VocabWord(
    val id: String,
    val word: String,
    val banglaMeaning: String,
    val pronunciation: String,
    val partOfSpeech: String,
    val definitionEn: String,
    val exampleEn: String,
    val exampleBn: String,
    val blankSentence: String,
    val dayGroup: Int = 1
)

object VocabularyContentBank {

    val allWords: List<VocabWord> = listOf(
        VocabWord(
            id = "v01",
            word = "Perseverance",
            banglaMeaning = "অধ্যবসায়",
            pronunciation = "/ˌpɜː.sɪˈvɪə.rəns/",
            partOfSpeech = "Noun",
            definitionEn = "Continued effort to achieve something despite difficulties or delay.",
            exampleEn = "Through quiet perseverance, she mastered both Arabic and English.",
            exampleBn = "নীরব অধ্যবসায়ের মাধ্যমে তিনি আরবি ও ইংরেজি উভয়ই আয়ত্ত করেছিলেন।",
            blankSentence = "Through quiet ___, she mastered both Arabic and English.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v02",
            word = "Serene",
            banglaMeaning = "প্রশান্ত, শান্ত",
            pronunciation = "/səˈriːn/",
            partOfSpeech = "Adjective",
            definitionEn = "Calm, peaceful, and untroubled.",
            exampleEn = "The mosque courtyard felt serene during the dawn prayer.",
            exampleBn = "ফজরের নামাজের সময় মসজিদের প্রাঙ্গণ ছিল অত্যন্ত প্রশান্ত।",
            blankSentence = "The mosque courtyard felt ___ during the dawn prayer.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v03",
            word = "Eloquent",
            banglaMeaning = "বাগ্মী, প্রাঞ্জল",
            pronunciation = "/ˈel.ə.kwənt/",
            partOfSpeech = "Adjective",
            definitionEn = "Fluent, clear, and persuasive in speaking or writing.",
            exampleEn = "He delivered an eloquent speech on the history of science.",
            exampleBn = "তিনি বিজ্ঞানের ইতিহাসের ওপর এক প্রাঞ্জল বক্তৃতা দিলেন।",
            blankSentence = "He delivered an ___ speech on the history of science.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v04",
            word = "Diligent",
            banglaMeaning = "পরিশ্রমী, যত্নশীল",
            pronunciation = "/ˈdɪl.ɪ.dʒənt/",
            partOfSpeech = "Adjective",
            definitionEn = "Showing careful and steady effort in work or study.",
            exampleEn = "A diligent student reviews vocabulary every evening.",
            exampleBn = "একজন পরিশ্রমী শিক্ষার্থী প্রতিদিন সন্ধ্যায় শব্দভান্ডার পুনরাবৃত্তি করে।",
            blankSentence = "A ___ student reviews vocabulary every evening.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v05",
            word = "Resilient",
            banglaMeaning = "সহনশীল, ঘুরে দাঁড়াতে সক্ষম",
            pronunciation = "/rɪˈzɪl.jənt/",
            partOfSpeech = "Adjective",
            definitionEn = "Able to recover quickly from difficult conditions.",
            exampleEn = "Coastal communities in Bangladesh are remarkably resilient.",
            exampleBn = "বাংলাদেশের উপকূলীয় জনগোষ্ঠী বিস্ময়করভাবে সহনশীল।",
            blankSentence = "Coastal communities in Bangladesh are remarkably ___.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v06",
            word = "Gratitude",
            banglaMeaning = "কৃতজ্ঞতা",
            pronunciation = "/ˈɡræt.ɪ.tjuːd/",
            partOfSpeech = "Noun",
            definitionEn = "The quality of being thankful and appreciating blessings.",
            exampleEn = "Starting the morning with gratitude brings peace to the heart.",
            exampleBn = "কৃতজ্ঞতার সাথে সকাল শুরু করলে হৃদয়ে শান্তি আসে।",
            blankSentence = "Starting the morning with ___ brings peace to the heart.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v07",
            word = "Cultivate",
            banglaMeaning = "গড়ে তোলা, চর্চা করা",
            pronunciation = "/ˈkʌl.tɪ.veɪt/",
            partOfSpeech = "Verb",
            definitionEn = "To develop a skill, habit, or quality intentionally over time.",
            exampleEn = "Daily reading helps cultivate patience and clarity of thought.",
            exampleBn = "নিয়মিত বই পড়া ধৈর্য এবং চিন্তার স্বচ্ছতা গড়ে তুলতে সাহায্য করে।",
            blankSentence = "Daily reading helps ___ patience and clarity of thought.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v08",
            word = "Lucid",
            banglaMeaning = "স্পষ্ট, সহজবোধ্য",
            pronunciation = "/ˈluː.sɪd/",
            partOfSpeech = "Adjective",
            definitionEn = "Expressed clearly and easy to understand.",
            exampleEn = "The author wrote a lucid explanation of a complex topic.",
            exampleBn = "লেখক একটি জটিল বিষয়ের সহজবোধ্য ব্যাখ্যা লিখেছেন।",
            blankSentence = "The author wrote a ___ explanation of a complex topic.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v09",
            word = "Integrity",
            banglaMeaning = "সততা, নৈতিক দৃঢ়তা",
            pronunciation = "/ɪnˈteɡ.rə.ti/",
            partOfSpeech = "Noun",
            definitionEn = "The quality of being honest and having strong moral principles.",
            exampleEn = "True leadership is built upon honesty and integrity.",
            exampleBn = "প্রকৃত নেতৃত্ব সততা ও নৈতিক দৃঢ়তার ওপর প্রতিষ্ঠিত।",
            blankSentence = "True leadership is built upon honesty and ___.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v10",
            word = "Contemplate",
            banglaMeaning = "গভীরভাবে চিন্তা করা",
            pronunciation = "/ˈkɒn.təm.pleɪt/",
            partOfSpeech = "Verb",
            definitionEn = "To think deeply and carefully about something.",
            exampleEn = "We should pause each day to contemplate our goals.",
            exampleBn = "আমাদের লক্ষ্যগুলো নিয়ে গভীরভাবে চিন্তা করতে প্রতিদিন একটু থামা উচিত।",
            blankSentence = "We should pause each day to ___ our goals.",
            dayGroup = 1
        ),
        VocabWord(
            id = "v11",
            word = "Benevolent",
            banglaMeaning = "দানশীল, পরোপকারী",
            pronunciation = "/bəˈnev.ə.lənt/",
            partOfSpeech = "Adjective",
            definitionEn = "Well-meaning, kind, and generous toward others.",
            exampleEn = "Benevolent patrons funded libraries across the region.",
            exampleBn = "পরোপকারী পৃষ্ঠপোষকরা অঞ্চলজুড়ে গ্রন্থাগার স্থাপনে অর্থায়ন করেছিলেন।",
            blankSentence = "___ patrons funded libraries across the region.",
            dayGroup = 2
        ),
        VocabWord(
            id = "v12",
            word = "Prudent",
            banglaMeaning = "বিচক্ষণ, দূরদর্শী",
            pronunciation = "/ˈpruː.dənt/",
            partOfSpeech = "Adjective",
            definitionEn = "Acting with care and thought for the future.",
            exampleEn = "It is prudent to save time for revision before exams.",
            exampleBn = "পরীক্ষার আগে পুনরাবৃত্তির জন্য সময় রাখা বিচক্ষণতার কাজ।",
            blankSentence = "It is ___ to save time for revision before exams.",
            dayGroup = 2
        ),
        VocabWord(
            id = "v13",
            word = "Fathom",
            banglaMeaning = "উপলব্ধি করা, গভীরতা বোঝা",
            pronunciation = "/ˈfæð.əm/",
            partOfSpeech = "Verb",
            definitionEn = "To understand a difficult problem or truth after much thought.",
            exampleEn = "It takes humility to fathom the vastness of knowledge.",
            exampleBn = "জ্ঞানের বিশালতা উপলব্ধি করতে বিনয়ের প্রয়োজন।",
            blankSentence = "It takes humility to ___ the vastness of knowledge.",
            dayGroup = 2
        ),
        VocabWord(
            id = "v14",
            word = "Steadfast",
            banglaMeaning = "অবিচল, দৃঢ়প্রতিজ্ঞ",
            pronunciation = "/ˈsted.fɑːst/",
            partOfSpeech = "Adjective",
            definitionEn = "Firmly loyal, constant, and unwavering in purpose.",
            exampleEn = "Remain steadfast in your daily habits even on busy days.",
            exampleBn = "ব্যস্ত দিনগুলোতেও আপনার দৈনন্দিন অভ্যাসে অবিচল থাকুন।",
            blankSentence = "Remain ___ in your daily habits even on busy days.",
            dayGroup = 2
        ),
        VocabWord(
            id = "v15",
            word = "Erudite",
            banglaMeaning = "পাণ্ডিত্যপূর্ণ, বিদ্বান",
            pronunciation = "/ˈer.jʊ.daɪt/",
            partOfSpeech = "Adjective",
            definitionEn = "Having or showing great scholarly knowledge.",
            exampleEn = "Al-Biruni was an erudite scholar of astronomy and geography.",
            exampleBn = "আল-বিরুনি জ্যোতির্বিজ্ঞান ও ভূগোলের একজন বিদ্বান পণ্ডিত ছিলেন।",
            blankSentence = "Al-Biruni was an ___ scholar of astronomy and geography.",
            dayGroup = 2
        )
    )

    fun dailyTenWords(): List<VocabWord> = allWords.take(10)
}
