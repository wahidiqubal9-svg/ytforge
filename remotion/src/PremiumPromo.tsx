import React from "react";
import {
  AbsoluteFill,
  Audio,
  Easing,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import { loadFont } from "@remotion/google-fonts/NotoSansDevanagari";

const { fontFamily: devanagari } = loadFont("normal", {
  weights: ["400", "700", "900"],
  subsets: ["devanagari", "latin"],
});

type Props = { audio: string };

const ACCENT = "#C8FF4D";
const VIOLET = "#8B5CF6";
const INK = "#F6F8FF";
const MUTED = "#93A0B8";
const CARD = "rgba(12,18,32,.72)";

const clamp = (v: number, min = 0, max = 1) => Math.min(max, Math.max(min, v));

const enter = (frame: number, fps: number, delay = 0, distance = 26) => {
  const p = spring({ frame: frame - delay, fps, config: { damping: 18, stiffness: 140 } });
  return {
    opacity: clamp(p),
    transform: `translateY(${interpolate(p, [0, 1], [distance, 0])}px)`,
  };
};

const Background: React.FC = () => {
  const frame = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();
  const t = frame / durationInFrames;
  return (
    <AbsoluteFill style={{ background: "#05070D", overflow: "hidden" }}>
      <div style={{
        position: "absolute", width: 620, height: 620, borderRadius: "50%",
        left: interpolate(t, [0, 1], [-220, 80]),
        top: interpolate(t, [0, 1], [-260, 110]),
        background: `radial-gradient(circle, ${VIOLET}55 0%, transparent 65%)`,
        filter: "blur(30px)",
      }} />
      <div style={{
        position: "absolute", width: 520, height: 520, borderRadius: "50%",
        right: interpolate(t, [0, 1], [-160, 180]),
        bottom: interpolate(t, [0, 1], [-220, 40]),
        background: `radial-gradient(circle, ${ACCENT}44 0%, transparent 65%)`,
        filter: "blur(34px)",
      }} />
      <div style={{
        position: "absolute", inset: 0,
        backgroundImage: "linear-gradient(rgba(255,255,255,.045) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.045) 1px, transparent 1px)",
        backgroundSize: "44px 44px",
        maskImage: "radial-gradient(circle at 50% 45%, black 0%, transparent 78%)",
      }} />
      <div style={{ position: "absolute", inset: 0, background: "linear-gradient(180deg, rgba(5,7,13,.18), rgba(5,7,13,.82))" }} />
      <div style={{ position: "absolute", inset: 0, opacity: 0.12, backgroundImage: "radial-gradient(#fff 0.6px, transparent 0.6px)", backgroundSize: "5px 5px" }} />
    </AbsoluteFill>
  );
};

const Kicker: React.FC<{ children: React.ReactNode; frame: number; fps: number; delay?: number }> = ({ children, frame, fps, delay = 0 }) => (
  <div style={{ ...enter(frame, fps, delay), color: ACCENT, fontSize: 11, fontWeight: 800, letterSpacing: 3, textTransform: "uppercase" }}>{children}</div>
);

const CodeCard: React.FC<{ code: string; frame: number; fps: number; big?: boolean }> = ({ code, frame, fps, big = false }) => (
  <div style={{
    ...enter(frame, fps, 8),
    display: "inline-flex", alignItems: "center", gap: 14,
    background: "rgba(200,255,77,.10)", border: "1px solid rgba(200,255,77,.45)",
    borderRadius: 14, padding: big ? "12px 20px" : "8px 14px",
    boxShadow: "0 0 34px rgba(200,255,77,.14)",
  }}>
    <span style={{ color: MUTED, fontSize: 9, fontWeight: 800, letterSpacing: 2 }}>USE CODE</span>
    <strong style={{ color: ACCENT, fontSize: big ? 34 : 22, lineHeight: 1, fontWeight: 900, letterSpacing: 1 }}>{code}</strong>
  </div>
);

const SceneWrap: React.FC<{ children: React.ReactNode; align?: "left" | "center" }> = ({ children, align = "left" }) => (
  <AbsoluteFill style={{ padding: "30px 42px 34px", justifyContent: "center", alignItems: align === "center" ? "center" : "flex-start", textAlign: align }}>
    {children}
  </AbsoluteFill>
);

const Hero: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap>
    <Kicker frame={frame} fps={fps} delay={3}>NEET PG · PREPLADDER</Kicker>
    <div style={{ ...enter(frame, fps, 8), marginTop: 10, fontSize: 52, lineHeight: 0.96, fontWeight: 900, letterSpacing: -2.6, maxWidth: 540 }}>
      तैयारी अब<br/><span style={{ color: ACCENT }}>महँगी नहीं.</span>
    </div>
    <div style={{ ...enter(frame, fps, 16), marginTop: 12, color: MUTED, fontSize: 16, lineHeight: 1.4 }}>
      PrepLadder One Plan · October offer
    </div>
    <div style={{ ...enter(frame, fps, 24), marginTop: 16 }}>
      <CodeCard code="P650" frame={frame} fps={fps} />
    </div>
  </SceneWrap>
);

const Plans: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap>
    <Kicker frame={frame} fps={fps} delay={2}>THE PLAN</Kicker>
    <div style={{ ...enter(frame, fps, 7), marginTop: 8, fontSize: 42, lineHeight: 1, fontWeight: 900, letterSpacing: -1.8 }}>
      12–66 <span style={{ color: ACCENT }}>महीने</span>
    </div>
    <div style={{ ...enter(frame, fps, 14), color: MUTED, fontSize: 15, marginTop: 8 }}>अपनी तैयारी के हिसाब से plan चुनें.</div>
    <div style={{ display: "flex", gap: 10, marginTop: 18, width: "100%", maxWidth: 520 }}>
      {["ONE PLAN", "MAX SAVINGS", "FARRE NOTES"].map((label, i) => (
        <div key={label} style={{
          ...enter(frame, fps, 20 + i * 6, 18),
          flex: 1, background: CARD, border: "1px solid rgba(255,255,255,.10)",
          borderRadius: 12, padding: "12px 10px", minWidth: 0,
        }}>
          <div style={{ color: i === 1 ? ACCENT : INK, fontSize: 15, fontWeight: 800, lineHeight: 1.1 }}>{label}</div>
          <div style={{ color: MUTED, fontSize: 9, letterSpacing: 1.2, marginTop: 6 }}>PREPLADDER</div>
        </div>
      ))}
    </div>
  </SceneWrap>
);

const Notes: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap>
    <Kicker frame={frame} fps={fps} delay={2}>BONUS</Kicker>
    <div style={{ ...enter(frame, fps, 8), marginTop: 8, fontSize: 50, lineHeight: 0.95, fontWeight: 900, letterSpacing: -2.2 }}>
      FARRE<br/><span style={{ color: ACCENT }}>NOTES FREE.</span>
    </div>
    <div style={{ ...enter(frame, fps, 16), marginTop: 12, color: MUTED, fontSize: 15, maxWidth: 480 }}>
      24 months या उससे ऊपर के plan पर revision notes बिल्कुल free.
    </div>
    <div style={{ ...enter(frame, fps, 24), marginTop: 18, display: "flex", gap: 10 }}>
      <div style={{ padding: "8px 12px", borderRadius: 99, background: "rgba(200,255,77,.10)", border: "1px solid rgba(200,255,77,.35)", color: ACCENT, fontSize: 11, fontWeight: 800 }}>24+ MONTHS</div>
      <div style={{ padding: "8px 12px", borderRadius: 99, background: "rgba(255,255,255,.06)", border: "1px solid rgba(255,255,255,.12)", color: INK, fontSize: 11, fontWeight: 700 }}>REVISION KIT</div>
    </div>
  </SceneWrap>
);

const CodeHero: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap align="center">
    <Kicker frame={frame} fps={fps} delay={2}>EXTRA SAVINGS</Kicker>
    <div style={{ ...enter(frame, fps, 8), marginTop: 10, fontSize: 24, fontWeight: 800, letterSpacing: -0.6 }}>Checkout पर लगाओ</div>
    <div style={{ ...enter(frame, fps, 14), marginTop: 8, fontSize: 92, lineHeight: 1, fontWeight: 900, letterSpacing: 2, color: ACCENT, textShadow: "0 0 50px rgba(200,255,77,.35)" }}>P650</div>
    <div style={{ ...enter(frame, fps, 22), color: MUTED, fontSize: 13 }}>और extra discount पाओ.</div>
  </SceneWrap>
);

const Steps: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap>
    <Kicker frame={frame} fps={fps} delay={2}>HOW TO USE</Kicker>
    <div style={{ ...enter(frame, fps, 7), marginTop: 6, fontSize: 34, fontWeight: 900, letterSpacing: -1.2 }}>3 steps. बस.</div>
    <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 16, width: "100%", maxWidth: 500 }}>
      {[
        ["01", "Plan पूरा करो"],
        ["02", "Checkout खोलो"],
        ["03", "P650 डालो → Apply"],
      ].map(([n, text], i) => (
        <div key={n} style={{
          ...enter(frame, fps, 14 + i * 7, 16),
          display: "flex", alignItems: "center", gap: 12,
          background: CARD, border: "1px solid rgba(255,255,255,.09)",
          borderRadius: 12, padding: "10px 12px",
        }}>
          <div style={{ color: ACCENT, fontSize: 13, fontWeight: 900, letterSpacing: 1 }}>{n}</div>
          <div style={{ color: INK, fontSize: 14, fontWeight: 700 }}>{text}</div>
        </div>
      ))}
    </div>
  </SceneWrap>
);

const Why: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap>
    <Kicker frame={frame} fps={fps} delay={2}>WHY PREPLADDER</Kicker>
    <div style={{ ...enter(frame, fps, 7), marginTop: 6, fontSize: 38, fontWeight: 900, letterSpacing: -1.6 }}>
      एक जगह. <span style={{ color: ACCENT }}>पूरी तैयारी.</span>
    </div>
    <div style={{ display: "flex", gap: 10, marginTop: 18, width: "100%", maxWidth: 520 }}>
      {[
        ["CONCEPTS", "Live + recorded"],
        ["PRACTICE", "QBank + tests"],
        ["REVISION", "FARRE notes"],
      ].map(([title, sub], i) => (
        <div key={title} style={{
          ...enter(frame, fps, 14 + i * 7, 20),
          flex: 1, background: CARD, border: "1px solid rgba(255,255,255,.09)",
          borderRadius: 12, padding: 12,
        }}>
          <div style={{ color: ACCENT, fontSize: 10, fontWeight: 800, letterSpacing: 1.5 }}>{title}</div>
          <div style={{ color: INK, fontSize: 13, fontWeight: 700, marginTop: 6 }}>{sub}</div>
        </div>
      ))}
    </div>
  </SceneWrap>
);

const Link: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap align="center">
    <Kicker frame={frame} fps={fps} delay={2}>GET THE DEAL</Kicker>
    <div style={{ ...enter(frame, fps, 8), marginTop: 10, fontSize: 34, fontWeight: 900, letterSpacing: -1.2 }}>
      medicalcoupons.in
    </div>
    <div style={{ ...enter(frame, fps, 16), marginTop: 10, color: MUTED, fontSize: 14 }}>सारे offers, एक जगह.</div>
    <div style={{ ...enter(frame, fps, 24), marginTop: 16 }}>
      <CodeCard code="P650" frame={frame} fps={fps} big />
    </div>
  </SceneWrap>
);

const Outro: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => (
  <SceneWrap align="center">
    <div style={{ ...enter(frame, fps, 2), fontSize: 12, fontWeight: 900, letterSpacing: 4, color: ACCENT }}>PREPLADDER</div>
    <div style={{ ...enter(frame, fps, 8), marginTop: 10, fontSize: 44, fontWeight: 900, letterSpacing: -1.4 }}>P650</div>
    <div style={{ ...enter(frame, fps, 14), color: MUTED, fontSize: 13, marginTop: 8 }}>Apply at checkout. Save more.</div>
  </SceneWrap>
);

export const PremiumPromo: React.FC<Props> = ({ audio }) => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();
  const segments = [
    { start: 0, end: 12, component: Hero },
    { start: 12, end: 32, component: Plans },
    { start: 32, end: 56, component: Notes },
    { start: 56, end: 78, component: CodeHero },
    { start: 78, end: 100, component: Steps },
    { start: 100, end: 118, component: Why },
    { start: 118, end: 136, component: Link },
    { start: 136, end: 146, component: Outro },
  ];
  const progress = interpolate(frame, [0, Math.min(durationInFrames, 146 * fps)], [0, 1], { extrapolateRight: "clamp" });

  return (
    <AbsoluteFill style={{ fontFamily: `${devanagari}, system-ui, -apple-system, sans-serif`, color: INK }}>
      <Background />
      {segments.map(({ start, end, component: Scene }, index) => {
        const from = Math.floor(start * fps);
        const duration = Math.max(1, Math.floor((end - start) * fps));
        const local = frame - from;
        if (local < 0 || local >= duration) return null;
        const fadeIn = clamp(local / 10);
        const fadeOut = clamp((duration - local) / 10);
        return (
          <AbsoluteFill key={index} style={{ opacity: Math.min(fadeIn, fadeOut) }}>
            <Scene frame={local} fps={fps} />
          </AbsoluteFill>
        );
      })}
      <div style={{ position: "absolute", left: 42, right: 42, bottom: 16, height: 2, background: "rgba(255,255,255,.12)", borderRadius: 99 }}>
        <div style={{ width: `${progress * 100}%`, height: "100%", background: ACCENT, borderRadius: 99, boxShadow: "0 0 10px rgba(200,255,77,.6)" }} />
      </div>
      <div style={{ position: "absolute", left: 42, bottom: 22, color: "rgba(255,255,255,.38)", fontSize: 8, letterSpacing: 2 }}>NEET PG · PREPLADDER · P650</div>
      <Audio src={staticFile(audio)} />
    </AbsoluteFill>
  );
};
