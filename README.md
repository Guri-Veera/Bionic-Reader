# Bionic Reader

An Android app that makes long text easier to read by **bolding the first part of every word** ("bionic reading"), giving your eyes an anchor so your brain can fill in the rest. Paste text or a link, or share an article straight from your browser, and read it in a clean, adjustable reader.

Built with Kotlin and Jetpack Compose. This is a prototype and is one half of a larger focus-helper app, alongside an impulse-purchase-control component.

## Features

- **Three ways in:**
  - Paste a link or plain text into the app
  - Share a page from Chrome (or any browser) to *Bionic Reader*
  - Select text in any app and choose *Bionic Reader* from the selection menu
- **Reader controls:** bionic on/off switch (for before/after comparison), bold amount slider, and text size slider
- **Load sample** button for a quick offline demo

## Requirements

- A recent stable **Android Studio** that supports Android Gradle Plugin 9.3.3 (if Studio shows an update prompt on open, accept it)
- **Android SDK Platform 37** (install via *Tools > SDK Manager > SDK Platforms*)
- Internet access on first sync, to download Gradle, dependencies, and the JDK the project requests
- A device or emulator running **Android 7.0 (API 24) or higher**

## Setup from GitHub

1. **Clone the repo.** In Android Studio: *File > New > Project from Version Control*, paste the URL, and choose a local folder:
   ```
   https://github.com/<your-username>/BionicReader.git
   ```
   Or clone with git and open the folder:
   ```
   git clone https://github.com/<your-username>/BionicReader.git
   ```
   then *File > Open* and select the `BionicReader` folder (the one containing `settings.gradle.kts`).
2. **Let Gradle sync.** Wait for the progress bar at the bottom to finish. The first sync can take several minutes. Android Studio creates `local.properties` with your SDK path automatically; it is not stored in the repo.
3. If Studio reports a missing SDK platform, click the link in the error (or open the SDK Manager) and install **Android SDK Platform 37**, then sync again.

## Run the app

**On an emulator**
1. Open *Tools > Device Manager* and click **Create Virtual Device**.
2. Pick a phone (for example *Pixel 7*) and a system image (API 34 or 35 with Google APIs is a stable choice), then finish.
3. Select the device in the toolbar dropdown and press the green **Run** button.

**On a physical phone**
1. Enable *Developer options* and *USB debugging* on the phone.
2. Plug it in, accept the debugging prompt, select it in the toolbar dropdown, and press **Run**.

## How to use the app

1. **Quick demo:** tap **Load sample**, then try the *Bionic* switch, the *Bold amount* slider, and the *Text size* slider.
2. **Paste something:** put an article link or some text in the box and tap **Read**.
3. **Share from a browser:** open an article in Chrome, tap the menu > *Share* > **Bionic Reader**. The app fetches the page and shows the article text.
4. **From selected text:** select text in any app and choose **Bionic Reader** from the selection menu (it may be under the overflow "⋮" button).
5. Tap **‹ Back** to return to the start screen.

## Project structure

```
BionicReader/
├── app/
│   ├── build.gradle.kts            App module config and dependencies
│   └── src/main/
│       ├── AndroidManifest.xml     Permissions + share / selected-text entry points
│       ├── java/com/example/bionic/
│       │   ├── BionicCore.kt       The bionic algorithm (pure Kotlin, easy to test or reuse)
│       │   ├── ArticleFetcher.kt   Downloads a page with Jsoup and extracts article text
│       │   └── MainActivity.kt     Compose UI and share / selected-text handling
│       └── res/                    App name, theme, launcher icons
├── gradle/                         Version catalog (libs.versions.toml) and Gradle wrapper
├── build.gradle.kts
└── settings.gradle.kts
```

### How it works

- `BionicCore.segments(text, intensity)` splits text into word and non-word pieces and marks the leading fraction of each word as bold. The UI turns those pieces into bold spans in an `AnnotatedString`.
- `ArticleFetcher` loads a URL with Jsoup, prefers the `<article>` element, and keeps paragraphs longer than about 40 characters.

## Known limitations

- Article extraction is a simple heuristic. It works on most blogs and news sites but can miss content on unusual layouts.
- Pages that need JavaScript to show their text, or that sit behind a paywall or login, won't load.
- The app activates only when you share, select, or paste text. It doesn't auto-convert pages as you browse.

## Troubleshooting

| Problem | Fix |
|---|---|
| Red errors that don't match the files on disk, or "conflicting overloads" | *File > Sync Project with Gradle Files*, then *File > Reload All from Disk*, then *File > Invalidate Caches > Invalidate and Restart* |
| "SDK location not found" | Open the project in Android Studio (it writes `local.properties`), or set `sdk.dir` in that file to your SDK path |
| Emulator shows a black screen | In *Device Manager*, edit the device, *Show Advanced Settings*, set **Graphics** to **Software**, then cold boot |
| "Pixel is already running as process …" | End the leftover `qemu-system` / `emulator` process in Task Manager, then *Cold Boot Now* in Device Manager |
| App crashes on launch with a class-not-found error | Make sure the manifest's `android:name` is `com.example.bionic.MainActivity` (the code package) |
| Gradle can't download the JDK or dependencies | Check your internet connection or proxy, then sync again |

Note: the code package is `com.example.bionic`, while the installed app id is `com.example.bionicreader` (set by `applicationId` in `app/build.gradle.kts`). The two are allowed to differ.

## Publishing this project to GitHub

From the project folder:

```
git init
git add .
git commit -m "Bionic Reader prototype"
git branch -M main
git remote add origin https://github.com/<your-username>/BionicReader.git
git push -u origin main
```

The included `.gitignore` keeps build output, `.idea/`, and your machine-specific `local.properties` out of the repo.
