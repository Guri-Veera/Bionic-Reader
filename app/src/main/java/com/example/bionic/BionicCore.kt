package com.example.bionic

import kotlin.math.ceil

/** Pure Kotlin (no Android/Compose imports) so it is easy to unit test and share later via KMP. */
data class Segment(val text: String, val bold: Boolean)

data class Article(val title: String, val paragraphs: List<String>)

object BionicCore {
    /** How many leading letters of a word to bold. intensity is 0.2..0.8. */
    fun boldLength(wordLength: Int, intensity: Float): Int {
        if (wordLength <= 1) return wordLength
        return ceil(wordLength * intensity).toInt().coerceIn(1, wordLength)
    }

    fun segments(text: String, intensity: Float = 0.5f): List<Segment> {
        val out = mutableListOf<Segment>()
        var i = 0
        while (i < text.length) {
            if (text[i].isLetterOrDigit()) {
                var j = i
                while (j < text.length &&
                    (text[j].isLetterOrDigit() || text[j] == '\'' || text[j] == '’')
                ) j++
                val word = text.substring(i, j)
                val n = boldLength(word.length, intensity)
                out += Segment(word.substring(0, n), true)
                if (n < word.length) out += Segment(word.substring(n), false)
                i = j
            } else {
                val start = i
                while (i < text.length && !text[i].isLetterOrDigit()) i++
                out += Segment(text.substring(start, i), false)
            }
        }
        return out
    }
}
