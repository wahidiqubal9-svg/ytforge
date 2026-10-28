# AGENTS.md — Video Production Instructions

> **STOP. Read this entire file before writing a script, generating audio, editing a composition, or rendering a video.**
>
> This repository is **video-only**. Never create, restore, build, or install an APK, Android app, Gradle project, or Android render backend.

## 1. What this repository does

- Produces short-form promotional videos with **Remotion**.
- Renders **only on GitHub Actions**, never on this Android/PRoot device.
- Uses **English for every word on screen**.
- Uses **Hindi for every voiceover**.
- Publishes the finished MP4 as a **public GitHub Release asset** and returns a clickable direct-download link.

## 2. Non-negotiable rules

1. **On-screen text is English only.** No Hindi/Devanagari text in titles, cards, captions, price labels, disclaimers, or end cards.
2. **Voiceover is Hindi.** The audio may be Hinglish in pronunciation, but the script language is Hindi.
3. **No APK/Android work.** Do not add Android, Gradle, Kotlin, or APK files.
4. **No local rendering.** Do not try to launch Chromium or Remotion locally; this device cannot run it. Push and render through GitHub Actions.
5. **No hardcoded scene timings.** Derive every scene boundary from the actual audio.
6. **Never invent data.** Prices, discounts, plan durations, claims, logos, and links must come from the user or a supplied source.
7. **Never commit secrets.** Sarvam/API keys live in GitHub Actions secrets only.
8. **No video is complete without a clickable MP4 link.**

## 3. Defaults when the user does not specify

- Aspect ratio: **16:9**
- Resolution: **1280×720**
- Frame rate: **30fps**
- Tone: **energetic, fast, premium**
- Voice: **Hindi, energetic**
- On-screen copy: **English, short, punchy**
- Length: fit the voiceover; do not pad with dead air
- Captions: English only
- Sound effects: yes, on transitions and key reveals
- Style: choose a style different from the previous two videos

If the user says “fast,” cut every 1.5–3 seconds and keep the voice pace around 1.1–1.2.

## 4. Required inputs

Before starting, collect:

- Brand name and logo file
- Offer details and exact price list
- Plan durations
- Coupon code and CTA link
- Target platform/aspect ratio
- Desired duration, if any
- Style direction or reference
- Voice preference, if any

If a price or claim is missing, ask. Do not fill the gap with a guess. Do not scrape a random price from the internet.

## 5. Project layout

Create one folder per video:

```text
projects/<slug>/
  video.json        # scenes, English text, Hindi voice lines, style, assets
  brief.md          # source brief and confirmed facts
  assets/           # logo, product images, screenshots
  voice/            # script, generated per-scene MP3s, timing data
```

Keep the Remotion composition under `remotion/src/`. Keep shared SFX under `remotion/public/sfx/`.

Do not put API keys, personal tokens, or private credentials in a project folder.

## 6. Script and voice workflow

1. Write the on-screen text in English.
2. Write the Hindi voiceover script separately.
3. Keep the two in the same scene objects in `video.json`.
4. Generate **one Hindi audio clip per scene**. Do not generate one long file and guess where the scenes fall.
5. Measure each clip with `ffprobe`.
6. Build `timing.json` containing each scene’s audio duration.
7. Set scene duration to `audio duration + 0.25–0.4s` of breathing room.
8. Place the scene’s text, price reveal, transition, and SFX inside that exact duration.

If the user supplies a finished MP3 instead of a script:

- Treat the MP3 as the source of truth.
- Split/align by actual speech boundaries.
- Do not reuse timings from an older video.
- If exact alignment is impossible, tell the user before rendering.

## 7. Sarvam voice generation

- Endpoint: `POST https://api.sarvam.ai/text-to-speech`
- Auth header: `api-subscription-key: ${{ secrets.SARVAM_API_KEY }}`
- Language: `hi-IN`
- Preferred model: `bulbul:v4-flash`, fallback `bulbul:v3`
- Prefer an energetic Hindi ad/reels persona
- Pace: about `1.1`–`1.2` for fast videos
- Output: MP3, 44.1kHz
- Generate per scene
- Never print the API key or full request headers in workflow logs

The key may be revoked or absent. If it is missing, stop and ask the user to add the `SARVAM_API_KEY` secret or provide an MP3. Do not commit a key.

## 8. Style system

Never use the same visual treatment twice in a row. Keep a record of the last two styles and pick a different one.

Available directions:

- Kinetic editorial
- Dark neon tech
- Glass/3D cards
- Magazine collage
- Cinematic letterbox
- Split-screen data story
- Minimal luxury
- Retro poster

Each video must use at least two advanced techniques, for example:

- 3D card rotation or parallax
- Masked/clip-path reveals
- Kinetic typography
- Camera push or pull
- Speed ramps
- Light sweeps
- Animated charts or counters
- Match cuts
- Layered SFX
- Motion blur or film grain

Do not copy the current composition’s layout into the next video. Reuse only the engine, not the look.

## 9. Sound design

- Voice is always the priority.
- Add SFX for transitions, price reveals, coupon reveals, and the final CTA.
- Use short whooshes, pops, clicks, dings, sparkles, risers, and impacts.
- Keep SFX under the voice; do not clip.
- No unlicensed music. If music is requested, use a licensed source and document it.
- Normalize the final mix to a consistent level.

## 10. Render pipeline

Use GitHub Actions only.

A render job must:

1. Check out the repository.
2. Install Node 22, ffmpeg, and Remotion.
3. Ensure the headless browser.
4. Generate/collect the Hindi audio.
5. Build timing data from actual audio durations.
6. Render a **360p preview** and a **720p final** (or the user’s requested resolution).
7. Inspect with `ffprobe`.
8. Create a release tag such as `video-<slug>-<run>`.
9. Publish the MP4 as a release asset.
10. Write the direct link to the workflow summary.

The current `pw-pulse-render.yml` is a working example. Use it as a template, not as a permanent single-purpose workflow.

## 11. Link delivery

The link must be a direct asset URL, not a release page:

```text
https://github.com/<owner>/<repo>/releases/download/<tag>/<file>.mp4
```

Before sending it:

- Confirm the file downloads.
- Confirm it is the intended resolution.
- Confirm audio is present.
- Confirm the duration is correct.
- Confirm the video contains English text and Hindi audio.

Send the link as a clickable markdown link and include a short spec line.

## 12. QA checklist

- [ ] English-only on-screen text
- [ ] Hindi voiceover only
- [ ] Audio and text aligned
- [ ] No missing fonts or tofu boxes
- [ ] Logo is the supplied/real logo
- [ ] Every price matches the confirmed source
- [ ] No invented plan or claim
- [ ] SFX present but not overpowering
- [ ] Style differs from the last two videos
- [ ] 16:9 720p unless requested otherwise
- [ ] Public direct MP4 link verified
- [ ] No secrets in the repository or logs

## 13. Current reference project

- Project: `projects/pw-meded/`
- Composition: `PwMedEdPulse`
- Audio: `remotion/public/voice-pw.mp3`
- Logo: `remotion/public/pw-meded-logo.png`
- SFX: `remotion/public/sfx/`
- Render workflow: `.github/workflows/pw-pulse-render.yml`

This is a reference for the pipeline, not a template to copy visually.
