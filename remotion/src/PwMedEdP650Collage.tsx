import React from "react";
import { AbsoluteFill, Audio, Img, Sequence, interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from "remotion";
import { loadFont as loadManrope } from "@remotion/google-fonts/Manrope";
import { loadFont as loadBebas } from "@remotion/google-fonts/BebasNeue";
import timing from "../public/p650-collage/timing.json";

const { fontFamily: body } = loadManrope("normal", { weights: ["400", "600", "800"], subsets: ["latin"] });
const { fontFamily: display } = loadBebas("normal", { weights: ["400"], subsets: ["latin"] });
const PAPER = "#FFF3D7";
const INK = "#171B2A";
const COLORS = ["#F05B45", "#EC3F71", "#0A8F83", "#F39A2E", "#5E55B8", "#1675A8"];

const scenes = [
  { id: "hook", tag: "COUPON ALERT", title: "THE CODE", accent: "TO REMEMBER", note: "P650", motif: "★" },
  { id: "promise", tag: "PW MEDED SAVINGS", title: "MAXIMUM", accent: "DISCOUNT", note: "ALL PLANS", motif: "↗" },
  { id: "diwali", tag: "FESTIVAL EDIT", title: "DIWALI", accent: "LIGHT UP", note: "YOUR SAVINGS", motif: "✦" },
  { id: "holi", tag: "FESTIVAL EDIT", title: "HOLI", accent: "ADD MORE", note: "COLOUR", motif: "●" },
  { id: "rakhi", tag: "FESTIVAL EDIT", title: "RAKSHA", accent: "BANDHAN", note: "A SMART GIFT", motif: "∞" },
  { id: "navratri", tag: "FESTIVAL EDIT", title: "NAVRATRI", accent: "ONE CODE", note: "NINE DAYS", motif: "✺" },
  { id: "independence", tag: "FESTIVAL EDIT", title: "INDEPENDENCE", accent: "DAY", note: "PLAN YOUR MOVE", motif: "↗" },
  { id: "steps", tag: "HOW TO USE IT", title: "CHOOSE", accent: "ENTER", note: "SAVE", motif: "01" },
  { id: "contact", tag: "GET THE DETAILS", title: "KNOW MORE", accent: "ONLINE", note: "WHATSAPP", motif: "@" },
  { id: "engage", tag: "JOIN THE CHANNEL", title: "COMMENT", accent: "SUBSCRIBE", note: "SHARE", motif: "♥" },
];

const frames = (seconds: number) => Math.max(1, Math.round(seconds * timing.fps));
const lengthFor = (index: number) => timing.scenes[index].audioSeconds + timing.breathingRoom;

const PaperTexture: React.FC = () => {
  const frame = useCurrentFrame();
  return <AbsoluteFill style={{ background: PAPER, overflow: "hidden" }}>
    <div style={{ position: "absolute", inset: 0, opacity: 0.38, backgroundImage: "radial-gradient(rgba(23,27,42,.12) .7px, transparent .7px)", backgroundSize: "5px 5px" }} />
    <div style={{ position: "absolute", inset: 0, opacity: 0.25, backgroundImage: "linear-gradient(90deg, rgba(23,27,42,.07) 1px, transparent 1px), linear-gradient(rgba(23,27,42,.07) 1px, transparent 1px)", backgroundSize: "58px 58px", transform: `translate(${(frame * .18) % 58}px, ${(frame * .12) % 58}px)` }} />
    <div style={{ position: "absolute", width: 620, height: 620, borderRadius: "50%", right: -230, top: -190, background: `${COLORS[0]}22` }} />
    <div style={{ position: "absolute", width: 420, height: 420, borderRadius: "50%", left: -190, bottom: -190, background: `${COLORS[2]}22` }} />
  </AbsoluteFill>;
};

const Header: React.FC = () => <div style={{ position: "absolute", left: 48, right: 48, top: 34, display: "flex", justifyContent: "space-between", alignItems: "center", zIndex: 10 }}>
  <div style={{ padding: "8px 13px", borderRadius: 7, background: "#fff", boxShadow: "0 8px 20px #171b2a22" }}><Img src={staticFile("pw-meded-logo.png")} style={{ width: 155, display: "block" }} /></div>
  <div style={{ fontFamily: body, fontSize: 11, fontWeight: 800, letterSpacing: 3, color: INK }}>FESTIVAL COLLAGE / P650</div>
</div>;

const Tape: React.FC<{ x: number; y: number; rotate: number; color: string }> = ({ x, y, rotate, color }) => <div style={{ position: "absolute", left: x, top: y, width: 118, height: 28, background: color, opacity: .78, transform: `rotate(${rotate}deg)`, boxShadow: "0 3px 7px #171b2a18" }} />;

const Doodle: React.FC<{ color: string; index: number }> = ({ color, index }) => {
  const frame = useCurrentFrame();
  const rotate = interpolate(frame, [0, 999], [-10, 18]);
  return <div style={{ position: "absolute", right: 95, bottom: 120, color, fontFamily: display, fontSize: 190, lineHeight: .7, transform: `rotate(${rotate + index * 7}deg)`, opacity: .84 }}>{scenes[index].motif}</div>;
};

const Sticker: React.FC<{ index: number; color: string }> = ({ index, color }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame: frame - 16, fps, config: { damping: 11, stiffness: 140 } });
  return <div style={{ position: "absolute", right: 116, top: 164, width: 190, height: 190, borderRadius: index % 2 ? 22 : "50%", background: color, color: PAPER, display: "flex", alignItems: "center", justifyContent: "center", textAlign: "center", fontFamily: body, fontSize: 16, fontWeight: 800, letterSpacing: 2, transform: `rotate(${index % 2 ? -8 : 10}deg) scale(${interpolate(p, [0, 1], [.4, 1])})`, opacity: Math.max(0, Math.min(1, p)), boxShadow: "14px 18px 0 #171b2a20" }}>{index === 0 ? "P650\nCODE" : index === 7 ? "STEP\nBY STEP" : index === 8 ? "GET\nDETAILS" : "FESTIVAL\nSAVINGS"}</div>;
};

const Scene: React.FC<{ index: number }> = ({ index }) => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();
  const scene = scenes[index];
  const color = COLORS[index % COLORS.length];
  const p = spring({ frame: frame - 5, fps, config: { damping: 16, stiffness: 125 } });
  const exit = interpolate(frame, [durationInFrames - 10, durationInFrames], [1, 0], { extrapolateLeft: "clamp", extrapolateRight: "clamp" });
  const progress = interpolate(frame, [0, durationInFrames], [0, 1], { extrapolateRight: "clamp" });
  return <AbsoluteFill style={{ opacity: exit }}>
    <Tape x={98} y={116} rotate={-4} color={`${color}88`} />
    <Tape x={1028} y={600} rotate={8} color={`${COLORS[(index + 2) % COLORS.length]}88`} />
    <Doodle color={color} index={index} />
    <Sticker index={index} color={color} />
    <div style={{ position: "absolute", left: 82, top: 157, width: 730, transform: `translateY(${interpolate(p, [0, 1], [38, 0])}px) rotate(${interpolate(p, [0, 1], [-2, 0])}deg)`, opacity: Math.max(0, Math.min(1, p)) }}>
      <div style={{ fontFamily: body, fontSize: 14, fontWeight: 800, letterSpacing: 4, color: COLORS[2] }}>{scene.tag}</div>
      <div style={{ marginTop: 17, fontFamily: display, fontSize: index === 6 ? 88 : 112, lineHeight: .82, letterSpacing: 1, color: INK }}>{scene.title}</div>
      <div style={{ marginTop: 2, fontFamily: display, fontSize: index === 6 ? 126 : 136, lineHeight: .83, letterSpacing: 1, color }}>{scene.accent}</div>
      <div style={{ marginTop: 23, display: "inline-flex", padding: "11px 17px", background: INK, color: PAPER, fontFamily: body, fontSize: 18, fontWeight: 800, letterSpacing: 2, transform: `rotate(${index % 2 ? 2 : -2}deg)` }}>{scene.note}</div>
      {index === 1 ? <div style={{ marginTop: 26, fontFamily: body, fontSize: 22, fontWeight: 800, color: INK }}>ONE CODE. EVERY PLAN.</div> : null}
      {index === 2 || index === 3 || index === 4 || index === 5 || index === 6 ? <div style={{ marginTop: 25, fontFamily: body, fontSize: 20, fontWeight: 800, color: INK }}>USE P650 BEFORE CHECKOUT</div> : null}
      {index === 7 ? <div style={{ marginTop: 25, display: "flex", gap: 12, fontFamily: body, fontSize: 18, fontWeight: 800, color: INK }}><span>01 CHOOSE</span><span>02 ENTER</span><span>03 APPLY</span></div> : null}
      {index === 8 ? <div style={{ marginTop: 25, fontFamily: body, fontSize: 19, fontWeight: 800, color: INK }}>medicalcoupons.in · 8942954415</div> : null}
      {index === 9 ? <div style={{ marginTop: 25, fontFamily: body, fontSize: 19, fontWeight: 800, color: INK }}>COMMENT · SUBSCRIBE · SHARE</div> : null}
    </div>
    <div style={{ position: "absolute", left: 80, right: 80, bottom: 45, height: 5, background: "#171b2a1c" }}><div style={{ height: "100%", width: `${progress * 100}%`, background: color }} /></div>
    <div style={{ position: "absolute", right: 82, bottom: 60, fontFamily: body, fontSize: 11, fontWeight: 800, letterSpacing: 3, color: INK }}>{String(index + 1).padStart(2, "0")} / 10</div>
  </AbsoluteFill>;
};

export const PwMedEdP650Collage: React.FC = () => {
  const { fps } = useVideoConfig();
  let from = 0;
  return <AbsoluteFill style={{ background: PAPER, color: INK, fontFamily: body }}>
    <PaperTexture />
    <Header />
    {scenes.map((scene, index) => {
      const duration = frames(lengthFor(index));
      const start = from;
      from += duration;
      return <Sequence key={scene.id} from={start} durationInFrames={duration}><Scene index={index} /><Audio src={staticFile(`p650-collage/voice/${scene.id}.mp3`)} /><Audio src={staticFile(index % 2 ? "sfx/pop.wav" : "sfx/whoosh.wav")} volume={0.22} /></Sequence>;
    })}
  </AbsoluteFill>;
};

export const p650CollageDurationInFrames = timing.scenes.reduce((total, scene) => total + frames(scene.audioSeconds + timing.breathingRoom), 0);
