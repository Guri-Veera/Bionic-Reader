package com.example.bionic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var incoming by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incoming = extractShared(intent)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    BionicApp(incoming) { incoming = null }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        incoming = extractShared(intent)
    }

    private fun extractShared(i: Intent?): String? = when (i?.action) {
        Intent.ACTION_SEND -> i.getStringExtra(Intent.EXTRA_TEXT)
        Intent.ACTION_PROCESS_TEXT -> i.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        else -> null
    }
}

private const val SAMPLE = """Reading long articles on a screen is hard when your attention keeps drifting. Bionic reading bolds the start of every word, giving your eyes an anchor so your brain can fill in the rest and keep moving.

This is a demo paragraph. Try the sliders to change how much of each word is bold, make the text larger, or switch bionic mode off to compare how the same text feels without it."""

@Composable
fun BionicApp(incoming: String?, onConsumed: () -> Unit) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var article by remember { mutableStateOf<Article?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var bionicOn by remember { mutableStateOf(true) }
    var intensity by remember { mutableFloatStateOf(0.5f) }
    var fontSize by remember { mutableFloatStateOf(18f) }

    fun load(raw: String) {
        scope.launch {
            loading = true; error = null
            val url = Regex("https?://\\S+").find(raw)?.value
            if (url != null) {
                ArticleFetcher.fetch(url)
                    .onSuccess { article = it }
                    .onFailure { error = it.message ?: "Couldn't load that page" }
            } else {
                val paras = raw.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotEmpty() }
                article = Article("Pasted text", paras)
            }
            loading = false
        }
    }

    LaunchedEffect(incoming) {
        incoming?.let { load(it); onConsumed() }
    }

    val a = article
    if (a == null) {
        Column(
            Modifier.fillMaxSize().padding(20.dp).statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Bionic Reader", style = MaterialTheme.typography.headlineMedium)
            Text("Paste a link or text, or share a page to this app from your browser.")
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                label = { Text("Link or text") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { load(input) }, enabled = input.isNotBlank() && !loading) { Text("Read") }
                OutlinedButton(onClick = { load(SAMPLE) }) { Text("Load sample") }
                if (loading) CircularProgressIndicator(Modifier.size(24.dp))
            }
        }
    } else {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { article = null }) { Text("‹ Back") }
                    Spacer(Modifier.weight(1f))
                    Text("Bionic")
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = bionicOn, onCheckedChange = { bionicOn = it })
                }
                Text("Bold amount")
                Slider(value = intensity, onValueChange = { intensity = it }, valueRange = 0.2f..0.8f, enabled = bionicOn)
                Text("Text size")
                Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 14f..28f)
            }
            HorizontalDivider()
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Text(a.title, style = MaterialTheme.typography.titleLarge) }
                items(a.paragraphs) { p ->
                    val text = if (bionicOn) {
                        buildAnnotatedString {
                            BionicCore.segments(p, intensity).forEach { s ->
                                if (s.bold) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(s.text) }
                                else append(s.text)
                            }
                        }
                    } else buildAnnotatedString { append(p) }
                    Text(text, fontSize = fontSize.sp, lineHeight = (fontSize * 1.6f).sp)
                }
            }
        }
    }
}
