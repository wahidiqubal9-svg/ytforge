# YTForge Video Studio

Agent-managed short-form videos built with **Remotion** and rendered on **GitHub Actions**.

- **On-screen text:** English only
- **Voiceover:** Hindi
- **Default output:** 16:9, 1280×720, 30fps
- **Delivery:** public direct-download MP4 links
- **No APK or Android build**

Read [AGENTS.md](AGENTS.md) before creating or rendering any video.

## Current video

**PW MedEd — Pulse**

[Download the 720p MP4](https://github.com/wahidiqubal9-svg/ytforge/releases/download/pw-pulse-1/pw-meded-pulse-720p.mp4)

- Composition: `PwMedEdPulse`
- Source project: `projects/pw-meded/`
- Voice: `remotion/public/voice-pw.mp3`
- Logo: `remotion/public/pw-meded-logo.png`
- SFX: `remotion/public/sfx/`

## Render

1. Push changes to `main`.
2. Open **Actions → PW MedEd Pulse Render**.
3. Run the workflow.
4. Open the workflow summary for the direct MP4 link.

## Voice generation

The Sarvam key belongs in the repository secret `SARVAM_API_KEY`. It must never be committed or printed in logs. If the key is unavailable, provide a Hindi MP3 and align the scenes to its measured durations.
