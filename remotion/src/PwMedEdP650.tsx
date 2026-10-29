import React from "react";
import {
  AbsoluteFill,
  Audio,
  Img,
  Sequence,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import { loadFont as loadManrope } from "@remotion/google-fonts/Manrope";
import { loadFont as loadBebas } from "@remotion/google-fonts/BebasNeue";
import timing from "../public/p650/timing.json";

const { fontFamily: body } = loadManrope("normal", { weights: ["400", "600", "800"], subsets: ["latin"] });
const { fontFamily: display } = loadBebas("normal", { weights: ["400"], subsets: ["latin"] });
const INK = "#17221C";
const CREAM = "#FFF8E9";
const SAFFRON = "#FF7A00";
const PINK = "#E83C72";
const TEAL = "#008C83";

const scenes = [
  { id: "hook", label: "THE SAVINGS CODE", title: "ONE CODE", accent: "MAXIMUM DISCOUNT", note: "PW MEDED" },
  { id: "occasion", label: "FESTIVE SAVINGS", title: "EVERY OCCASION", accent: "ONE SMART CODE", note: "DIWALI · HOLI · RAKSHA BANDHAN" },
  { id: "all-plans", label: "PLAN FLEXIBILITY", title: "ALL PLANS", accent: "USE P650", note: "APPLY AT CHECKOUT" },
  { id: "remember", label: "YOUR CHECKOUT REMINDER", title: "REMEMBER", accent: "P650", note: "MAXIMUM DISCOUNT" },
  { id: "contact", label: "GET THE DETAILS", title: "KNOW MORE", accent: "MEDICALCOUPONS.IN", note: "WHATSAPP 8942954415" },
  { id: "engage", label: "JOIN THE COMMUNITY", title: "COMMENT BELOW", accent: "SUBSCRIBE NOW", note: "PW MEDED COUPON UPDATES" },
];

const secondsToFrames = (seconds: number, fps: number) => Math.max(1, Math.round(seconds * fps));
const sceneLength = (index: number) => timing.scenes[index].audioSeconds + timing.breathingRoom;

const Background: React.FC = () => {
  const frame = useCurrentFrame();
  return (
    <AbsoluteFill style={{ background: CREAM, overflow: "hidden" }}>
      <div style={{ position: "absolute", inset: 0, background: `linear-gradient(120deg, ${CREAM} 0%, #FFF0D2 52%, #FFE3D9 100%)` }} />
      <div style={{ position: "absolute", width: 650, height: 650, borderRadius: "50%", right: -220, top: -180, background: `${SAFFRON}20`, filter: "blur(4px)", transform: `rotate(${frame * 0.15}deg)` }} />
      <div style={{ position: "absolute", width: 440, height: 440, borderRadius: "50%", left: -170, bottom: -160, background: `${TEAL}18` }} />
      <div style={{ position: "absolute", inset: 0, opacity: 0.26, backgroundImage: "linear-gradient(90deg, rgba(23,34,28,.06) 1px, transparent 1px), linear-gradient(rgba(23,34,28,.06) 1px, transparent 1px)", backgroundSize: "44px 44px" }} />
      <div style={{ position: "absolute", inset: 0, background: "radial-gradient(circle at 50% 50%, transparent 35%, rgba(23,34,28,.10) 100%)" }} />
    </AbsoluteFill>
  );
};

const Header: React.FC = () => (
  <div style={{ position: "absolute", top: 34, left: 52, right: 52, display: "flex", justifyContent: "space-between", alignItems: "center", zIndex: 5 }}>
    <div style={{ padding: "8px 14px", background: "white", borderRadius: 6, boxShadow: "0 10px 30px rgba(23,34,28,.12)" }}>
      <Img src={staticFile("pw-meded-logo.png")} style={{ width: 158, display: "block" }} />
    </div>
    <div style={{ fontFamily: body, fontSize: 11, fontWeight: 800, letterSpacing: 3, color: TEAL }}>PW MEDED / P650</div>
  </div>
);

const Scene: React.FC<{ index: number }> = ({ index }) => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();
  const scene = scenes[index];
  const reveal = spring({ frame, fps, config: { damping: 16, stiffness: 130 } });
  const progress = interpolate(frame, [0, durationInFrames], [0, 1], { extrapolateRight: "clamp" });
  const occasion = index === 1;
  return (
    <AbsoluteFill style={{ padding: "132px 78px 72px", color: INK }}>
      <div style={{ position: "absolute", right: 80, top: 142, width: 320, height: 320, borderRadius: "50%", border: `2px solid ${index % 2 ? PINK : SAFFRON}55`, transform: `scale(${0.72 + reveal * 0.28}) rotate(${frame * (index + 1) * 0.18}deg)` }} />
      <div style={{ position: "absolute", right: 110, top: 172, width: 250, height: 250, borderRadius: "50%", border: `1px dashed ${TEAL}66`, transform: `rotate(${-frame * 0.25}deg)` }} />
      <div style={{ width: 730, ...{ opacity: reveal, transform: `translateY(${interpolate(reveal, [0, 1], [28, 0])}px)` } }}>
        <div style={{ fontFamily: body, fontSize: 13, fontWeight: 800, letterSpacing: 4, color: TEAL }}>{scene.label}</div>
        <div style={{ marginTop: 18, fontFamily: display, fontSize: 104, lineHeight: 0.9, letterSpacing: 1 }}>{scene.title}</div>
        <div style={{ marginTop: 6, fontFamily: display, fontSize: 120, lineHeight: 0.88, color: index % 2 ? PINK : SAFFRON, letterSpacing: 1 }}>{scene.accent}</div>
        <div style={{ marginTop: 28, display: "inline-flex", padding: "12px 18px", background: INK, color: CREAM, fontFamily: body, fontSize: occasion ? 15 : 17, fontWeight: 800, letterSpacing: occasion ? 1.5 : 2 }}>{scene.note}</div>
      </div>
      {occasion ? <div style={{ position: "absolute", right: 72, bottom: 118, width: 390, fontFamily: body, fontSize: 18, lineHeight: 1.5, fontWeight: 600, color: INK }}>Diwali · Holi · Raksha Bandhan · Navratri · Independence Day</div> : null}
      <div style={{ position: "absolute", left: 78, right: 78, bottom: 52, height: 4, background: "rgba(23,34,28,.13)" }}><div style={{ width: `${progress * 100}%`, height: "100%", background: index % 2 ? PINK : SAFFRON }} /></div>
      <div style={{ position: "absolute", right: 78, bottom: 68, fontFamily: body, fontSize: 11, fontWeight: 800, letterSpacing: 3, color: TEAL }}>{String(index + 1).padStart(2, "0")} / 06</div>
    </AbsoluteFill>
  );
};

export const PwMedEdP650: React.FC = () => {
  const { fps } = useVideoConfig();
  let from = 0;
  return (
    <AbsoluteFill style={{ fontFamily: body }}>
      <Background />
      <Header />
      {scenes.map((scene, index) => {
        const duration = secondsToFrames(sceneLength(index), fps);
        const start = from;
        from += duration;
        return <Sequence key={scene.id} from={start} durationInFrames={duration}><Scene index={index} /><Audio src={staticFile(`p650/voice/${scene.id}.mp3`)} /></Sequence>;
      })}
      <Audio src={staticFile("sfx/whoosh.wav")} volume={0.35} />
    </AbsoluteFill>
  );
};

export const p650DurationInFrames = timing.scenes.reduce((sum, scene) => sum + secondsToFrames(sceneLength(timing.scenes.indexOf(scene)), timing.fps), 0);
