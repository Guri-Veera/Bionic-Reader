package com.example.bionic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object ArticleFetcher {
    suspend fun fetch(url: String): Result<Article> = withContext(Dispatchers.IO) {
        runCatching {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .timeout(10_000)
                .get()
            // Simple heuristic: prefer <article>, fall back to <body>; keep substantial paragraphs.
            val root = doc.selectFirst("article") ?: doc.body()
            val paras = root.select("p").map { it.text().trim() }.filter { it.length > 40 }
            require(paras.isNotEmpty()) { "Couldn't find readable text on this page" }
            Article(doc.title().ifBlank { url }, paras)
        }
    }
}
