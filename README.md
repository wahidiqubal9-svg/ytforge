# YTForge

Chat-driven YouTube video studio for Android.

**Status:** bootstrap. The on-device Chromium/HyperFrames runtime was ruled out after a
PRoot benchmark (Chromium crashes with SIGTRAP). Rendering moves to a remote HyperFrames
engine, with a local WebView preview path for the app.

- App package: `com.wahidiqubal.ytforge`
- Target: Android 13+, 720p output
- Builds: GitHub Actions only
- Secrets: API keys are entered in the app, never stored in this repository

## Render spike

The `Render Spike` workflow renders a 10-second 720x1280 HyperFrames composition on a
GitHub-hosted runner and uploads the MP4 as a build artifact.
