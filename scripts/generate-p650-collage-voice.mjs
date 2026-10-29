import fs from "node:fs";
import { execFileSync } from "node:child_process";

const root = new URL("..", import.meta.url).pathname;
const video = JSON.parse(fs.readFileSync(`${root}/projects/pw-meded-p650-festival-collage/video.json`, "utf8"));
const out = `${root}/remotion/public/p650-collage/voice`;
fs.mkdirSync(out, { recursive: true });

const requestAudio = (text, model) => {
  const payload = JSON.stringify({
    inputs: [text],
    target_language_code: "hi-IN",
    model,
    speaker: "shubh",
    pace: 1.12,
    speech_sample_rate: 44100,
    enable_preprocessing: true,
  });
  const response = execFileSync("curl", [
    "--fail-with-body", "-sS", "-X", "POST", "https://api.sarvam.ai/text-to-speech",
    "-H", `api-subscription-key: ${process.env.SARVAM_API_KEY}`,
    "-H", "Content-Type: application/json",
    "--data", payload,
  ], { encoding: "utf8", maxBuffer: 64 * 1024 * 1024 });
  const audio = JSON.parse(response).audios?.[0];
  if (!audio) throw new Error("Sarvam returned no audio");
  return Buffer.from(audio, "base64");
};

const synthesize = (text) => {
  try {
    return requestAudio(text, "bulbul:v4-flash");
  } catch {
    return requestAudio(text, "bulbul:v3");
  }
};

const scenes = [];
for (const scene of video.scenes) {
  const file = `${out}/${scene.id}.mp3`;
  fs.writeFileSync(file, synthesize(scene.voice));
  const seconds = Number(execFileSync("ffprobe", ["-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", file], { encoding: "utf8" }).trim());
  scenes.push({ id: scene.id, audioSeconds: seconds });
  console.log(`Measured ${scene.id}: ${seconds.toFixed(3)}s`);
}
fs.writeFileSync(`${root}/remotion/public/p650-collage/timing.json`, `${JSON.stringify({ fps: 30, breathingRoom: 0.35, scenes }, null, 2)}\n`);
