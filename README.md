
# Birthday LLM Video (Android)

An Android sample app that lets you pick images and a music track, then builds a vertical 720×1280 slideshow video and merges the soundtrack — all on‑device using FFmpeg‑Kit. You can also call the OpenAI API for text prompts (e.g., generate birthday wishes).

## Features
- Select multiple pictures and a music file from storage.
- Generate a slideshow: each photo stays on screen 1.5 s.
- Merge MP3 into the video (AAC 128 kb/s).
- Uses **FFmpeg‑Kit** (GPL build) for encoding.
- Kotlin code, minSdk 24, target 34.
- OpenAI client snippet included (needs `OPENAI_API_KEY` in your env).

## Build
1. Import into **Android Studio Giraffe** or newer.
2. Ensure you have Google’s `cmdline-tools` and Android 34 SDK.
3. Click **Run ▶**.

## Note
- FFmpeg‑Kit adds ~40 MB to the APK; for production consider the `min-gpl` or `audio` variant.
- Runtime encoding is CPU‑intensive — test on modern devices.
"# birthday-video-maker" 
