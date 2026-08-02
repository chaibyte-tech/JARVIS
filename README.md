# JARVIS V2

A lightweight Android voice-assistant prototype with a Jarvis-inspired animated interface, hands-free wake phrase while the app is active, speech recognition, Cloudflare Worker routing and spoken responses.

## Use
Open once, grant microphone permission, then say **Hey Jarvis** followed by a command. The app continuously restarts Android speech recognition while it remains active. Android may stop microphone access when the app is backgrounded; true system-level hotword behavior requires a default-assistant/VoiceInteractionService or dedicated on-device wake-word engine.

## APK
Actions → Build JARVIS APK → latest successful run → Artifacts → JARVIS-debug-apk.
