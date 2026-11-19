package com.example.data

import android.util.Xml
import com.example.data.local.CachedNewsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.util.concurrent.TimeUnit

data class RssFeedSource(
    val sourceName: String,
    val urls: List<String>, // Primary + fallback RSS URLs
    val region: String, // BANGLADESH or INTERNATIONAL
    val defaultCategory: String,
    val maxItems: Int = 6
)

class NewsRssService {

    companion object {
        private val HTML_TAG_REGEX = Regex("<[^>]*>")
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val NUMERIC_ENTITY_REGEX = Regex("&#(x?[0-9a-fA-F]+);")

        val bangladeshChannels = listOf(
            "All",
            "Jamuna TV",
            "Channel 24",
            "Ekattor TV",
            "Independent TV",
            "RTV News",
            "The Daily Star",
            "The Daily Campus"
        )
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val feedSources = listOf(
        // Requested Bangladesh News Channels (Google News site-scoped RSS + direct fallbacks)
        RssFeedSource(
            sourceName = "Jamuna TV",
            urls = listOf(
                "https://news.google.com/rss/search?q=site:jamuna.tv&hl=bn&gl=BD&ceid=BD:bn",
                "https://jamuna.tv/feed"
            ),
            region = "BANGLADESH",
            defaultCategory = "National"
        ),
        RssFeedSource(
            sourceName = "Channel 24",
            urls = listOf(
                "https://news.google.com/rss/search?q=site:channel24bd.tv&hl=bn&gl=BD&ceid=BD:bn"
            ),
            region = "BANGLADESH",
            defaultCategory = "National"
        ),
        RssFeedSource(
            sourceName = "Ekattor TV",
            urls = listOf(
                "https://news.google.com/rss/search?q=site:ekattor.tv&hl=bn&gl=BD&ceid=BD:bn"
            ),
            region = "BANGLADESH",
            defaultCategory = "National"
        ),
        RssFeedSource(
            sourceName = "Independent TV",
            urls = listOf(
                "https://news.google.com/rss/search?q=site:independent24.com&hl=bn&gl=BD&ceid=BD:bn"
            ),
            region = "BANGLADESH",
            defaultCategory = "National"
        ),
        RssFeedSource(
            sourceName = "RTV News",
            urls = listOf(
                "https://news.google.com/rss/search?q=site:rtvonline.com&hl=bn&gl=BD&ceid=BD:bn"
            ),
            region = "BANGLADESH",
            defaultCategory = "National"
        ),
        RssFeedSource(
            sourceName = "The Daily Star",
            urls = listOf(
                "https://www.thedailystar.net/frontpage/rss.xml",
                "https://news.google.com/rss/search?q=site:thedailystar.net&hl=en-BD&gl=BD&ceid=BD:en"
            ),
            region = "BANGLADESH",
            defaultCategory = "National"
        ),
        RssFeedSource(
            sourceName = "The Daily Campus",
            urls = listOf(
                "https://news.google.com/rss/search?q=site:thedailycampus.com&hl=bn&gl=BD&ceid=BD:bn"
            ),
            region = "BANGLADESH",
            defaultCategory = "Education"
        ),
        // International News Feeds
        RssFeedSource(
            sourceName = "BBC World News",
            urls = listOf("https://feeds.bbci.co.uk/news/world/rss.xml"),
            region = "INTERNATIONAL",
            defaultCategory = "World Affairs",
            maxItems = 10
        ),
        RssFeedSource(
            sourceName = "Al Jazeera",
            urls = listOf("https://www.aljazeera.com/xml/rss/all.xml"),
            region = "INTERNATIONAL",
            defaultCategory = "International Politics",
            maxItems = 10
        ),
        RssFeedSource(
            sourceName = "BBC Technology",
            urls = listOf("https://feeds.bbci.co.uk/news/technology/rss.xml"),
            region = "INTERNATIONAL",
            defaultCategory = "Science & Tech",
            maxItems = 8
        )
    )

    suspend fun fetchLiveNews(): Result<List<CachedNewsEntity>> = withContext(Dispatchers.IO) {
        val perFeedResults = coroutineScope {
            feedSources.map { feed ->
                async { fetchSingleFeed(feed) }
            }.awaitAll()
        }

        val collected = mutableListOf<CachedNewsEntity>()
        val seenHeadlines = mutableSetOf<String>()

        // Interleave items across feeds so Home & News tabs show diverse channels first
        val maxLen = perFeedResults.maxOfOrNull { it.size } ?: 0
        for (i in 0 until maxLen) {
            for (list in perFeedResults) {
                val article = list.getOrNull(i) ?: continue
                val normalized = article.title.lowercase().trim()
                if (normalized.isNotBlank() && seenHeadlines.add(normalized)) {
                    collected.add(article)
                }
            }
        }

        if (collected.isNotEmpty()) {
            Result.success(collected)
        } else {
            Result.failure(IllegalStateException("Unable to reach live RSS news feeds. Please check your internet connection."))
        }
    }

    private fun fetchSingleFeed(feed: RssFeedSource): List<CachedNewsEntity> {
        for (url in feed.urls) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36")
                    .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string().orEmpty()
                        if (body.isNotBlank() && (body.contains("<item", ignoreCase = true) || body.contains("<entry", ignoreCase = true))) {
                            val parsed = parseFeedXml(body, feed)
                            if (parsed.isNotEmpty()) return parsed
                        }
                    }
                }
            } catch (_: Exception) {
                // Try next fallback URL for this channel
            }
        }
        return emptyList()
    }

    private fun parseFeedXml(xmlText: String, feed: RssFeedSource): List<CachedNewsEntity> {
        val items = mutableListOf<CachedNewsEntity>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlText))

        var inEntry = false
        var title = ""
        var description = ""
        var link = ""
        var pubDate = ""
        var imageUrl = ""
        var category = feed.defaultCategory

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT && items.size < feed.maxItems) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.lowercase()
                    when (tag) {
                        "item", "entry" -> {
                            inEntry = true
                            title = ""
                            description = ""
                            link = ""
                            pubDate = ""
                            imageUrl = ""
                            category = feed.defaultCategory
                        }
                        "title" -> if (inEntry) title = cleanHeadline(decodeHtml(safeNextText(parser)))
                        "description", "summary", "content" -> if (inEntry && description.isBlank()) {
                            description = decodeHtml(safeNextText(parser))
                        }
                        "link" -> if (inEntry && link.isBlank()) {
                            val href = parser.getAttributeValue(null, "href").orEmpty()
                            link = if (href.startsWith("http")) href else safeNextText(parser)
                        }
                        "pubdate", "published", "updated" -> if (inEntry && pubDate.isBlank()) {
                            pubDate = safeNextText(parser)
                        }
                        "category" -> if (inEntry) {
                            val parsedCat = parser.getAttributeValue(null, "term") ?: safeNextText(parser)
                            if (parsedCat.isNotBlank()) category = mapCategory(parsedCat, feed.defaultCategory)
                        }
                        "media:thumbnail", "media:content", "enclosure" -> if (inEntry && imageUrl.isBlank()) {
                            val urlAttr = parser.getAttributeValue(null, "url").orEmpty()
                            if (urlAttr.startsWith("http")) imageUrl = urlAttr
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.lowercase()
                    if ((tag == "item" || tag == "entry") && inEntry) {
                        inEntry = false
                        if (title.isNotBlank() && link.isNotBlank()) {
                            val cleanSummary = description
                                .takeIf { it.isNotBlank() && !it.equals(title, ignoreCase = true) }
                                ?.take(220)
                                ?: "Tap Full Article to read the complete report from ${feed.sourceName}."

                            items.add(
                                CachedNewsEntity(
                                    id = "${feed.region}_${feed.sourceName}_${link.hashCode()}",
                                    title = title.trim(),
                                    summary = cleanSummary,
                                    source = feed.sourceName,
                                    url = link.trim(),
                                    imageUrl = imageUrl,
                                    publishedAt = formatShortPubDate(pubDate),
                                    category = inferCategoryFromTitle(title, category),
                                    region = feed.region,
                                    bookmarked = false,
                                    cachedAtMillis = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return items
    }

    private fun safeNextText(parser: XmlPullParser): String {
        return try {
            parser.nextText().orEmpty().trim()
        } catch (_: Exception) {
            ""
        }
    }

    private fun decodeHtml(raw: String): String {
        if (raw.isBlank()) return ""
        val withoutTags = HTML_TAG_REGEX.replace(raw, " ")
        val decodedNamed = withoutTags
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&ndash;", "–")
            .replace("&mdash;", "—")
            .replace("&lsquo;", "‘")
            .replace("&rsquo;", "’")
            .replace("&ldquo;", "“")
            .replace("&rdquo;", "”")
        val decodedNumeric = NUMERIC_ENTITY_REGEX.replace(decodedNamed) { match ->
            val token = match.groupValues[1]
            val code = if (token.startsWith("x", ignoreCase = true)) {
                token.substring(1).toIntOrNull(16)
            } else {
                token.toIntOrNull(10)
            }
            if (code != null && code in 32..0x10FFFF) {
                String(Character.toChars(code))
            } else {
                " "
            }
        }
        return WHITESPACE_REGEX.replace(decodedNumeric, " ").trim()
    }

    /**
     * Strips trailing Google News " - Publisher Name" suffix so headlines render cleanly.
     */
    private fun cleanHeadline(raw: String): String {
        val lastDash = raw.lastIndexOf(" - ")
        return if (lastDash > 18) raw.substring(0, lastDash).trim() else raw.trim()
    }

    private fun formatShortPubDate(raw: String): String {
        if (raw.isBlank()) return "Recent"
        return raw.replace(" GMT", "").replace(" +0000", "").take(22)
    }

    private fun mapCategory(raw: String, fallback: String): String {
        val lower = raw.lowercase()
        return when {
            lower.contains("sport") || lower.contains("cricket") -> "Sports"
            lower.contains("econ") || lower.contains("business") || lower.contains("market") -> "Economy"
            lower.contains("tech") || lower.contains("science") -> "Science & Tech"
            lower.contains("climat") || lower.contains("environ") -> "Climate"
            lower.contains("edu") || lower.contains("campus") -> "Education"
            lower.contains("polit") || lower.contains("govt") -> "Politics"
            else -> fallback
        }
    }

    private fun inferCategoryFromTitle(title: String, current: String): String {
        if (current != "National" && current != "World Affairs") return current
        val lower = title.lowercase()
        return when {
            lower.contains("cricket") || lower.contains("football") || lower.contains("খেলা") || lower.contains("ক্রিকেট") -> "Sports"
            lower.contains("bank") || lower.contains("taka") || lower.contains("economy") || lower.contains("অর্থনীতি") || lower.contains("বাজার") -> "Economy"
            lower.contains("university") || lower.contains("exam") || lower.contains("শিক্ষা") || lower.contains("বিশ্ববিদ্যালয়") || lower.contains("ভর্তি") -> "Education"
            lower.contains("climate") || lower.contains("flood") || lower.contains("cyclone") || lower.contains("আবহাওয়া") || lower.contains("বৃষ্টি") -> "Climate"
            lower.contains("tech") || lower.contains("প্রযুক্তি") -> "Science & Tech"
            else -> current
        }
    }
}
