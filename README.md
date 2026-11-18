# PulseMusic — Android Music Player (Apple Music UI + YouTube Music Connect)

PulseMusic is a clean, high-speed Android music app built with **Kotlin**, **Jetpack Compose**, and **AndroidX Media3 (ExoPlayer)**.

## Features Included
- **Apple Music-Style UI:** Deep OLED dark theme, dynamic atmospheric gradients, smooth artwork scale animations (expands on play, shrinks on pause), and floating mini-player.
- **Pressure-Touch Tactile Feel:** Spring-physics press animations (`PressureTouch.kt`) paired with crisp haptic feedback.
- **Zero-Latency Audio Engine (`ZeroLatencyAudioEngine.kt`):**
  - 250 MB LRU disk cache + persistent HTTP/2 connections
  - 250 ms playback start buffer
  - Automatic **next-track pre-buffering** so switching songs feels instant
- **Free Ad-Free Music Out of the Box:**
  - **Free Ad-Free Tab:** Streams trending and searchable songs via the public [Audius API](https://audius.org/) (no API key needed).
  - **On Device Tab:** Plays local MP3/FLAC songs stored on your Android phone with 0 ms latency.
- **Connect YouTube Music Tab (Official API):**
  - Connects your Google / YouTube account via official OAuth 2.0 (`youtube.readonly`).
  - Syncs your YouTube / YouTube Music playlists and lets you search tracks using the free [YouTube Data API v3](https://developers.google.com/youtube/data/determine_quota_cost) (100 searches/day + 10,000 units/day free).
  - Plays tracks inside PulseMusic using the official embedded YouTube IFrame Player without opening the YouTube Music app.

---

## Step-by-Step: How to Run on Your Android Phone (Using Mac)

### Step 1: Open in Android Studio
1. Download and install [Android Studio](https://developer.android.com/studio) on your Mac.
2. Unzip `PulseMusic-Android.zip`.
3. Open Android Studio -> click **Open** -> select the `PulseMusic` folder.
4. Wait 1–2 minutes for Gradle Sync to finish.

### Step 2: Run on Your Android Phone
1. On your Android phone: **Settings -> About phone -> tap Build number 7 times** to enable Developer Options.
2. Go to **Developer Options -> turn on USB Debugging**.
3. Plug your phone into your Mac with a USB cable and tap **Allow** on the phone popup.
4. In Android Studio, click the green **Run (▶)** button at the top.

### Step 3: Activate "Connect YouTube Music" (Free, 5 Minutes)
1. Open [Google Cloud Console](https://console.cloud.google.com/) and create a new project (e.g., `PulseMusic`).
2. Go to **APIs & Services -> Library**, search for **YouTube Data API v3**, and click **Enable**.
3. Go to **APIs & Services -> Credentials -> Create Credentials -> API Key**. Copy that key.
4. Also click **Create Credentials -> OAuth client ID -> Android**:
   - Package name: `com.pulsemusic.app`
   - SHA-1 fingerprint: run `./gradlew signingReport` inside Android Studio's Terminal and copy the `SHA1` under `debug`.
5. Open `app/build.gradle.kts` in Android Studio and replace:
   - `YOUR_YOUTUBE_DATA_API_V3_KEY` with your API Key
   - `YOUR_GOOGLE_OAUTH_CLIENT_ID` with your OAuth Client ID
6. Click **Sync Now** and hit **Run (▶)** again — now tap **YouTube Music -> Connect** inside the app!
