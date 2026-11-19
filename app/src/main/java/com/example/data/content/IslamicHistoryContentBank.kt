package com.example.data.content

data class HistoryTimelineEvent(
    val dateLabel: String,
    val eventBn: String
)

data class HistoryQuizQuestion(
    val questionBn: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationBn: String
)

data class IslamicHistoryLesson(
    val id: String,
    val category: String,
    val titleBn: String,
    val titleEn: String,
    val eraLabel: String,
    val sourceTypeBadge: String, // e.g., "Established Historical Record & Authentic Hadith"
    val references: List<String>,
    val overviewBn: String,
    val keyPersonalities: List<String>,
    val timeline: List<HistoryTimelineEvent>,
    val significanceBn: String,
    val summaryBn: String,
    val mcqs: List<HistoryQuizQuestion>,
    val shortAnswerPrompts: List<String>
)

object IslamicHistoryContentBank {

    val categories = listOf(
        "All",
        "Life of Prophet Muhammad (PBUH)",
        "Rightly Guided Caliphs",
        "Abbasid Caliphate & Golden Age",
        "Muslim Scientists & Scholars",
        "History of Previous Prophets",
        "Umayyad Caliphate"
    )

    val lessons: List<IslamicHistoryLesson> = listOf(
        IslamicHistoryLesson(
            id = "hist_hijrah",
            category = "Life of Prophet Muhammad (PBUH)",
            titleBn = "মদীনায় হিজরত ও মদীনা সনদ",
            titleEn = "The Hijrah & Constitution of Madinah",
            eraLabel = "622 CE (1 AH)",
            sourceTypeBadge = "Quran, Authentic Hadith & Established Historical Record",
            references = listOf(
                "Sahih al-Bukhari (Kitab Manaqib al-Ansar)",
                "Sirat Ibn Hisham",
                "Ar-Raheeq Al-Makhtum (Safiur Rahman Mubarakpuri)"
            ),
            overviewBn = "মক্কায় ১৩ বছর দাওয়াত ও চরম নির্যাতন সহ্য করার পর ৬২২ খ্রিস্টাব্দে রাসূলুল্লাহ (সা.) মহান আল্লাহর নির্দেশে আবু বকর (রা.)-কে সাথে নিয়ে মদীনায় হিজরত করেন। মদীনায় পৌঁছে তিনি প্রথমেই মসজিদে নববী নির্মাণ করেন, মুহাজির ও আনসারদের মাঝে ভ্রাতৃত্ব (মুয়াখাত) স্থাপন করেন এবং মদীনায় বসবাসরত সকল গোত্র ও ধর্মাবলম্বীদের অধিকার ও দায়িত্ব নির্ধারণ করে ঐতিহাসিক 'মদীনা সনদ' (সহীফাতুল মদীনা) প্রণয়ন করেন।",
            keyPersonalities = listOf(
                "Prophet Muhammad (PBUH)",
                "Abu Bakr as-Siddiq (RA)",
                "Ali ibn Abi Talib (RA)",
                "Asma bint Abi Bakr (RA)"
            ),
            timeline = listOf(
                HistoryTimelineEvent("622 CE (Rabi' al-Awwal)", "মক্কা থেকে সওর গুহা হয়ে মদীনায় নিরাপদ আগমন।"),
                HistoryTimelineEvent("1 AH (Quba)", "মদীনার উপকণ্ঠে কুবা মসজিদ নির্মাণ—ইসলামের ইতিহাসে প্রথম মসজিদ।"),
                HistoryTimelineEvent("1 AH (Madinah)", "মসজিদে নববী প্রতিষ্ঠা ও আনসার-মুহাজির ভ্রাতৃত্ব বন্ধন।"),
                HistoryTimelineEvent("1 AH", "মদীনা সনদ প্রণয়ন—বহুধর্মীয় সমাজে ন্যায়বিচার ও শান্তিপূর্ণ সহাবস্থানের দলিল।")
            ),
            significanceBn = "হিজরত কেবল একটি ভৌগোলিক স্থানান্তর ছিল না; এটি ইসলামী সভ্যতা, ন্যায়ভিত্তিক সমাজব্যবস্থা এবং হিজরী সনের সূচনা করে। মদীনা সনদকে ইতিহাসের অন্যতম প্রাচীন লিখিত সংবিধান হিসেবে গণ্য করা হয়।",
            summaryBn = "হিজরত (৬২২ খ্রি.) ইসলামী ইতিহাসের মোড় ঘুরিয়ে দেয়। মসজিদকেন্দ্রিক সমাজগঠন, ভ্রাতৃত্ব ও মদীনা সনদের মাধ্যমে মদীনা একটি সুশৃঙ্খল ও ন্যায়ভিত্তিক রাষ্ট্র হিসেবে গড়ে ওঠে।",
            mcqs = listOf(
                HistoryQuizQuestion(
                    questionBn = "হিজরতের সফরসঙ্গী হিসেবে রাসূলুল্লাহ (সা.)-এর সাথে সওর গুহায় কে ছিলেন?",
                    options = listOf("উমর (রা.)", "আবু বকর (রা.)", "উসমান (রা.)", "তালহা (রা.)"),
                    correctIndex = 1,
                    explanationBn = "আবু বকর (রা.) হিজরতের সফরে রাসূলুল্লাহ (সা.)-এর সঙ্গী ছিলেন, যার উল্লেখ পবিত্র কুরআনের সূরা তাওবার ৪০ নং আয়াতে রয়েছে।"
                ),
                HistoryQuizQuestion(
                    questionBn = "মদীনার উপকণ্ঠে নির্মিত ইসলামের প্রথম মসজিদ কোনটি?",
                    options = listOf("মসজিদে কিবলাতাইন", "মসজিদে কুবা", "মসজিদে গামামা", "মসজিদে জুমা"),
                    correctIndex = 1,
                    explanationBn = "হিজরতের পথে মদীনার উপকণ্ঠে কুবা নামক স্থানে মসজিদে কুবা নির্মিত হয়।"
                ),
                HistoryQuizQuestion(
                    questionBn = "হিজরত কোন খ্রিস্টাব্দে সংঘটিত হয়েছিল?",
                    options = listOf("৬১০ খ্রি.", "৬১৫ খ্রি.", "৬২২ খ্রি.", "৬৩০ খ্রি."),
                    correctIndex = 2,
                    explanationBn = "ঐতিহাসিকভাবে সুপ্রতিষ্ঠিত যে হিজরত ৬২২ খ্রিস্টাব্দে সংঘটিত হয়।"
                ),
                HistoryQuizQuestion(
                    questionBn = "মদীনা সনদের মূল উদ্দেশ্য কী ছিল?",
                    options = listOf("শুধু বাণিজ্য চুক্তি", "সকল গোত্র ও নাগরিকের অধিকার, নিরাপত্তা ও ন্যায়বিচার নিশ্চিত করা", "কর আদায় করা", "নগর প্রাচীর নির্মাণ"),
                    correctIndex = 1,
                    explanationBn = "মদীনা সনদ সকল গোত্র ও ধর্মাবলম্বীর পারস্পরিক নিরাপত্তা, ধর্মীয় স্বাধীনতা ও ন্যায়বিচারের ভিত্তি স্থাপন করে।"
                ),
                HistoryQuizQuestion(
                    questionBn = "মক্কা থেকে হিজরত করে আসা সাহাবীদের কী বলা হয়?",
                    options = listOf("আনসার", "মুহাজির", "তাবেয়ী", "খলিফা"),
                    correctIndex = 1,
                    explanationBn = "মক্কা থেকে হিজরতকারীদের 'মুহাজির' এবং মদীনার সাহায্যকারী সাহাবীদের 'আনসার' বলা হয়।"
                )
            ),
            shortAnswerPrompts = listOf(
                "মদীনা সনদ কীভাবে একটি বহুধর্মীয় সমাজে শান্তি ও ন্যায়বিচার প্রতিষ্ঠার উদাহরণ তৈরি করেছিল?",
                "আনসার ও মুহাজিরদের মধ্যকার ভ্রাতৃত্ব (মুয়াখাত) থেকে আজকের সমাজ কী শিক্ষা নিতে পারে?"
            )
        ),
        IslamicHistoryLesson(
            id = "hist_golden_age",
            category = "Abbasid Caliphate & Golden Age",
            titleBn = "বাগদাদের বাইতুল হিকমাহ ও ইসলামী স্বর্ণযুগ",
            titleEn = "Bayt al-Hikmah & The Islamic Golden Age",
            eraLabel = "8th – 13th Century CE",
            sourceTypeBadge = "Established Historical Record",
            references = listOf(
                "Al-Bidayah wan-Nihayah (Ibn Kathir)",
                "Tarikh al-Tabari",
                "Jim Al-Khalili — The House of Wisdom (Historical Survey)"
            ),
            overviewBn = "আব্বাসীয় খিলাফতের আমলে, বিশেষত খলিফা হারুন আর-রশীদ এবং তাঁর পুত্র আল-মামুনের পৃষ্ঠপোষকতায় বাগদাদে 'বাইতুল হিকমাহ' (জ্ঞানের ঘর) এক বিশ্বমানের গবেষণা কেন্দ্র ও গ্রন্থাগার হিসেবে বিকশিত হয়। এখানে গ্রিক, পারস্য ও ভারতীয় বিজ্ঞানের মূল্যবান গ্রন্থ আরবিতে অনুবাদ করা হয় এবং গণিত, জ্যোতির্বিজ্ঞান, চিকিৎসাশাস্ত্র ও রসায়নে মৌলিক গবেষণা শুরু হয়।",
            keyPersonalities = listOf(
                "Muhammad ibn Musa al-Khwarizmi",
                "Al-Kindi",
                "Banu Musa Brothers",
                "Hunayn ibn Ishaq"
            ),
            timeline = listOf(
                HistoryTimelineEvent("762 CE", "খলিফা আল-মনসুর কর্তৃক বাগদাদ নগরী প্রতিষ্ঠা।"),
                HistoryTimelineEvent("813–833 CE", "খলিফা আল-মামুনের আমলে বাইতুল হিকমাহ-এর সর্বোচ্চ বিকাশ।"),
                HistoryTimelineEvent("~820 CE", "আল-খোয়ারিজমি কর্তৃক 'আল-জাবর ওয়াল-মুকাবালা' রচনা—বীজগণিতের ভিত্তি।"),
                HistoryTimelineEvent("1258 CE", "মঙ্গোল আক্রমণে বাগদাদ ও বাইতুল হিকমাহ-এর ধ্বংস।")
            ),
            significanceBn = "বাইতুল হিকমাহ কেবল প্রাচীন জ্ঞান সংরক্ষণই করেনি, বরং আধুনিক অ্যালগরিদম, বীজগণিত (Algebra) এবং পরীক্ষামূলক বৈজ্ঞানিক পদ্ধতির পথ প্রশস্ত করেছে।",
            summaryBn = "আব্বাসীয় আমলে বাগদাদের বাইতুল হিকমাহ ছিল বিশ্বের অন্যতম প্রধান জ্ঞানচর্চার কেন্দ্র, যেখানে অনুবাদ ও মৌলিক গবেষণার মাধ্যমে মানবসভ্যতা বহু শতাব্দী এগিয়ে গিয়েছিল।",
            mcqs = listOf(
                HistoryQuizQuestion(
                    questionBn = "বাইতুল হিকমাহ কোন নগরীতে অবস্থিত ছিল?",
                    options = listOf("দামেস্ক", "বাগদাদ", "কায়রো", "কর্ডোবা"),
                    correctIndex = 1,
                    explanationBn = "বাইতুল হিকমাহ আব্বাসীয় রাজধানী বাগদাদে অবস্থিত ছিল।"
                ),
                HistoryQuizQuestion(
                    questionBn = "বীজগণিত (Algebra)-এর জনক হিসেবে পরিচিত কোন মুসলিম বিজ্ঞানী?",
                    options = listOf("ইবনে সিনা", "আল-খোয়ারিজমি", "আল-রাজি", "ইবনে বতুতা"),
                    correctIndex = 1,
                    explanationBn = "আল-খোয়ারিজমির 'আল-জাবর' গ্রন্থ থেকেই Algebra শব্দের উৎপত্তি।"
                ),
                HistoryQuizQuestion(
                    questionBn = "কোন আব্বাসীয় খলিফার আমলে বাইতুল হিকমাহ সর্বোচ্চ প্রসার লাভ করে?",
                    options = listOf("আল-মামুন", "উমর ইবনে আবদুল আজিজ", "মুয়াবিয়া (রা.)", "সুলাইমান"),
                    correctIndex = 0,
                    explanationBn = "খলিফা আল-মামুন (৮১৩–৮৩৩ খ্রি.) বাইতুল হিকমাহকে প্রধান গবেষণা ও অনুবাদ কেন্দ্রে রূপান্তরিত করেন।"
                ),
                HistoryQuizQuestion(
                    questionBn = "Algorithm শব্দটি কোন বিজ্ঞানীর নামের লাতিন রূপ থেকে এসেছে?",
                    options = listOf("আল-বিরুনি", "আল-খোয়ারিজমি", "ইবনুল হাইসাম", "জাবির ইবনে হাইয়ান"),
                    correctIndex = 1,
                    explanationBn = "আল-খোয়ারিজমির নামের লাতিন রূপ 'Algoritmi' থেকে Algorithm শব্দটি এসেছে।"
                ),
                HistoryQuizQuestion(
                    questionBn = "১২৫৮ খ্রিস্টাব্দে কারা বাগদাদ আক্রমণ করে বাইতুল হিকমাহ ধ্বংস করে?",
                    options = listOf("রোমানরা", "মঙ্গোলরা (হালাকু খান)", "ভাইকিংরা", "পর্তুগিজরা"),
                    correctIndex = 1,
                    explanationBn = "১২৫৮ খ্রিস্টাব্দে হালাকু খানের নেতৃত্বে মঙ্গোল বাহিনী বাগদাদ ধ্বংস করে।"
                )
            ),
            shortAnswerPrompts = listOf(
                "বাইতুল হিকমাহ-এর অনুবাদ আন্দোলন কীভাবে বিশ্ববিজ্ঞানের অগ্রগতিতে ভূমিকা রেখেছিল?",
                "আধুনিক কম্পিউটার বিজ্ঞানে আল-খোয়ারিজমির অবদান সংক্ষেপে ব্যাখ্যা করুন।"
            )
        ),
        IslamicHistoryLesson(
            id = "hist_ibn_al_haytham",
            category = "Muslim Scientists & Scholars",
            titleBn = "ইবনুল হাইসাম ও আধুনিক বৈজ্ঞানিক পদ্ধতি",
            titleEn = "Ibn al-Haytham & The Scientific Method",
            eraLabel = "965 – c. 1040 CE",
            sourceTypeBadge = "Established Historical Record",
            references = listOf(
                "Kitab al-Manazir (Book of Optics)",
                "UNESCO International Year of Light (2015) Historical Dossier"
            ),
            overviewBn = "আবু আলী আল-হাসান ইবনুল হাইসাম (পশ্চিমে Alhazen নামে পরিচিত) ছিলেন বসরা শহরে জন্মগ্রহণকারী একজন শ্রেষ্ঠ পদার্থবিজ্ঞানী, গণিতবিদ ও জ্যোতির্বিজ্ঞানী। তিনি তাঁর বিখ্যাত গ্রন্থ 'কিতাবুল মানাজির' (Book of Optics)-এ প্রমাণ করেন যে চোখ থেকে আলো বের হয় না, বরং কোনো বস্তু থেকে প্রতিফলিত আলো আমাদের চোখে প্রবেশ করলে আমরা তা দেখতে পাই।",
            keyPersonalities = listOf("Ibn al-Haytham (Alhazen)"),
            timeline = listOf(
                HistoryTimelineEvent("965 CE", "ইরাকের বসরা শহরে জন্মগ্রহণ।"),
                HistoryTimelineEvent("1011–1021 CE", "কায়রোতে অবস্থানকালে 'কিতাবুল মানাজির' (৭ খণ্ড) রচনা।"),
                HistoryTimelineEvent("c. 1040 CE", "কায়রোতে ইন্তেকাল; তাঁর কাজ ইউরোপীয় রেনেসাঁয় গভীর প্রভাব ফেলে।")
            ),
            significanceBn = "পর্যবেক্ষণ, অনুমান, নিয়ন্ত্রিত পরীক্ষা ও গাণিতিক প্রমাণের মাধ্যমে সত্য যাচাই করার আধুনিক বৈজ্ঞানিক পদ্ধতির (Scientific Method) অন্যতম প্রথম প্রবক্তা ছিলেন ইবনুল হাইসাম। ক্যামেরা অবস্কুরা (Camera Obscura) ব্যাখ্যায় তাঁর অবদান আধুনিক ফটোগ্রাফির ভিত্তি।",
            summaryBn = "ইবনুল হাইসাম আলোকবিজ্ঞান (Optics) এবং পরীক্ষামূলক বৈজ্ঞানিক পদ্ধতির পথিকৃৎ। তাঁর 'কিতাবুল মানাজির' শতাব্দীর পর শতাব্দী বিশ্ববিজ্ঞানের পাঠ্যবই ছিল।",
            mcqs = listOf(
                HistoryQuizQuestion(
                    questionBn = "ইবনুল হাইসামের সবচেয়ে বিখ্যাত গ্রন্থের নাম কী?",
                    options = listOf("আল-কানুন ফিত-তিব্ব", "কিতাবুল মানাজির", "মুকাদ্দিমা", "কিতাবুল হিন্দ"),
                    correctIndex = 1,
                    explanationBn = "'কিতাবুল মানাজির' (Book of Optics) আলোকবিজ্ঞানের যুগান্তকারী গ্রন্থ।"
                ),
                HistoryQuizQuestion(
                    questionBn = "ইবনুল হাইসাম দৃষ্টির বিষয়ে কোন সত্যটি প্রমাণ করেন?",
                    options = listOf("চোখ থেকে রশ্মি বের হয়", "বস্তু থেকে আলো চোখে প্রবেশ করে", "আলো সরলরেখায় চলে না", "অন্ধকারে চোখ আলো তৈরি করে"),
                    correctIndex = 1,
                    explanationBn = "তিনি পরীক্ষা-নিরীক্ষার মাধ্যমে প্রমাণ করেন যে বস্তু থেকে প্রতিফলিত আলো চোখে প্রবেশ করলেই দর্শন ঘটে।"
                ),
                HistoryQuizQuestion(
                    questionBn = "পশ্চিমা বিশ্বে ইবনুল হাইসাম কী নামে পরিচিত?",
                    options = listOf("Avicenna", "Averroes", "Alhazen", "Geber"),
                    correctIndex = 2,
                    explanationBn = "ইউরোপীয় গ্রন্থে তাঁর নাম লাতিন উচ্চারণে 'Alhazen' লেখা হয়েছে।"
                ),
                HistoryQuizQuestion(
                    questionBn = "কোন যন্ত্রটির মূলনীতি ব্যাখ্যা করে তিনি আধুনিক ক্যামেরার পথ দেখান?",
                    options = listOf("টেলিস্কোপ", "ক্যামেরা অবস্কুরা (পিনহোল ক্যামেরা)", "কম্পাস", "ব্যারোমিটার"),
                    correctIndex = 1,
                    explanationBn = "তিনি অন্ধকার কক্ষে ক্ষুদ্র ছিদ্র দিয়ে আলোর প্রতিবিম্ব গঠনের (Camera Obscura) সঠিক ব্যাখ্যা দেন।"
                ),
                HistoryQuizQuestion(
                    questionBn = "ইবনুল হাইসাম কোন শহরে জন্মগ্রহণ করেন?",
                    options = listOf("বসরা", "সমরকন্দ", "গ্রানাডা", "দিল্লি"),
                    correctIndex = 0,
                    explanationBn = "তিনি ৯৬৫ খ্রিস্টাব্দে ইরাকের বসরা শহরে জন্মগ্রহণ করেন।"
                )
            ),
            shortAnswerPrompts = listOf(
                "ইবনুল হাইসামের পরীক্ষামূলক পদ্ধতি আজকের বিজ্ঞান গবেষণার সাথে কীভাবে মিলে যায়?",
                "আলোকবিজ্ঞানে 'কিতাবুল মানাজির'-এর একটি মূল অবদান লিখুন।"
            )
        ),
        IslamicHistoryLesson(
            id = "hist_khulafa",
            category = "Rightly Guided Caliphs",
            titleBn = "খুলাফায়ে রাশেদীন ও ন্যায়পরায়ণ শাসনব্যবস্থা",
            titleEn = "The Four Rightly Guided Caliphs",
            eraLabel = "632 – 661 CE",
            sourceTypeBadge = "Authentic Hadith & Established Historical Record",
            references = listOf(
                "Tarikh al-Khulafa (Jalaluddin al-Suyuti)",
                "Al-Bidayah wan-Nihayah (Ibn Kathir)"
            ),
            overviewBn = "রাসূলুল্লাহ (সা.)-এর ইন্তেকালের পর ইসলামের চারজন শ্রেষ্ঠ সাহাবী—আবু বকর (রা.), উমর ইবনুল খাত্তাব (রা.), উসমান ইবনে আফফান (রা.) এবং আলী ইবনে আবি তালিব (রা.)—পর্যায়ক্রমে খিলাফতের দায়িত্ব পালন করেন। এই ৩০ বছরব্যাপী সময়কালকে 'খিলাফতে রাশেদা' বলা হয়, যা শূরা (পরামর্শ), জবাবদিহিতা ও ন্যায়বিচারের উজ্জ্বল দৃষ্টান্ত।",
            keyPersonalities = listOf(
                "Abu Bakr as-Siddiq (RA) — 632–634 CE",
                "Umar ibn al-Khattab (RA) — 634–644 CE",
                "Uthman ibn Affan (RA) — 644–656 CE",
                "Ali ibn Abi Talib (RA) — 656–661 CE"
            ),
            timeline = listOf(
                HistoryTimelineEvent("632–634 CE", "আবু বকর (রা.): মুসলিম উম্মাহর ঐক্য রক্ষা ও প্রথমবার কুরআন সংকলন।"),
                HistoryTimelineEvent("634–644 CE", "উমর (রা.): হিজরী সন প্রবর্তন, বাইতুল মাল ও বিচার বিভাগের প্রাতিষ্ঠানিক রূপদান।"),
                HistoryTimelineEvent("644–656 CE", "উসমান (রা.): কুরআনের প্রামাণ্য কপি (মুসহাফে উসমানী) বিশ্বব্যাপী প্রেরণ।"),
                HistoryTimelineEvent("656–661 CE", "আলী (রা.): জ্ঞান, প্রজ্ঞা ও ন্যায়বিচারের আদর্শ সমুন্নত রাখা।")
            ),
            significanceBn = "খুলাফায়ে রাশেদীনের শাসনামলে শাসকের ব্যক্তিগত বিলাসিতা বর্জন, সাধারণ নাগরিকের অধিকার সুরক্ষা এবং আইনের শাসন প্রতিষ্ঠার যে মানদণ্ড স্থাপিত হয়, তা বিশ্বইতিহাসে বিরল।",
            summaryBn = "খুলাফায়ে রাশেদীনের যুগ (৬৩২–৬৬১ খ্রি.) কুরআন সংরক্ষণ, হিজরী সন প্রবর্তন এবং ন্যায়ভিত্তিক জনকল্যাণমূলক রাষ্ট্রপরিচালনার সোনালি অধ্যায়।",
            mcqs = listOf(
                HistoryQuizQuestion(
                    questionBn = "কোন খলিফার আমলে প্রথম পবিত্র কুরআন একত্রিত করে সংকলন করা হয়?",
                    options = listOf("আবু বকর (রা.)", "উমর (রা.)", "আলী (রা.)", "মুয়াবিয়া (রা.)"),
                    correctIndex = 0,
                    explanationBn = "আবু বকর (রা.)-এর আমলে উমর (রা.)-এর পরামর্শে যায়েদ ইবনে সাবিত (রা.)-এর নেতৃত্বে প্রথম কুরআন সংকলিত হয়।"
                ),
                HistoryQuizQuestion(
                    questionBn = "হিজরী সন প্রবর্তন করেন কোন খলিফা?",
                    options = listOf("আবু বকর (রা.)", "উমর ইবনুল খাত্তাব (রা.)", "উসমান (রা.)", "আলী (রা.)"),
                    correctIndex = 1,
                    explanationBn = "খলিফা উমর (রা.) সাহাবীদের সাথে পরামর্শ করে হিজরতের বছর থেকে হিজরী সন গণনা শুরু করেন।"
                ),
                HistoryQuizQuestion(
                    questionBn = "কুরআনের প্রামাণ্য প্রতিলিপি (মুসহাফ) বিভিন্ন প্রদেশে প্রেরণ করেন কে?",
                    options = listOf("উসমান ইবনে আফফান (রা.)", "আবু বকর (রা.)", "উমর (রা.)", "খালিদ (রা.)"),
                    correctIndex = 0,
                    explanationBn = "উসমান (রা.) উম্মাহর পঠনপদ্ধতির ঐক্য রক্ষায় প্রামাণ্য মুসহাফ প্রস্তুত করে বিভিন্ন প্রদেশে পাঠান।"
                ),
                HistoryQuizQuestion(
                    questionBn = "খুলাফায়ে রাশেদীনের সময়কাল কত বছর স্থায়ী ছিল?",
                    options = listOf("প্রায় ১০ বছর", "প্রায় ৩০ বছর", "প্রায় ১০০ বছর", "প্রায় ৫ বছর"),
                    correctIndex = 1,
                    explanationBn = "৬৩২ খ্রিস্টাব্দ থেকে ৬৬১ খ্রিস্টাব্দ পর্যন্ত প্রায় ৩০ বছর খিলাফতে রাশেদা স্থায়ী ছিল।"
                ),
                HistoryQuizQuestion(
                    questionBn = "খুলাফায়ে রাশেদীনের শাসনব্যবস্থার অন্যতম মূলনীতি কী ছিল?",
                    options = listOf("বংশানুক্রমিক রাজতন্ত্র", "শূরা (পরামর্শ) ও জবাবদিহিতা", "বিলাসবহুল প্রাসাদ", "একনায়কতন্ত্র"),
                    correctIndex = 1,
                    explanationBn = "শূরা বা পরামর্শ এবং জনগণের প্রতি জবাবদিহিতা ছিল খিলাফতে রাশেদার মূল বৈশিষ্ট্য।"
                )
            ),
            shortAnswerPrompts = listOf(
                "উমর (রা.)-এর প্রশাসনিক সংস্কারগুলোর মধ্যে দুটি উল্লেখযোগ্য উদ্যোগের নাম লিখুন।",
                "খিলাফতে রাশেদা থেকে আধুনিক নেতৃত্বের জন্য কী শিক্ষা রয়েছে?"
            )
        )
    )
}
