import React from "react";
import {
  AbsoluteFill,
  Audio,
  Easing,
  Img,
  Sequence,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";

export type PromoProps = {
  headline: string;
  subheadline: string;
  code: string;
  cta: string;
  title: string;
  styleId: string;
  ratio: string;
  image?: string;
  audio?: string;
  durationInFrames?: number;
};

type Palette = {
  bgA: string;
  bgB: string;
  accent: string;
  ink: string;
  muted: string;
  card: string;
  codeBg: string;
  codeInk: string;
  light?: boolean;
};

const palettes: Record<string, Palette> = {
  kinetic: { bgA: "#07111F", bgB: "#14395C", accent: "#3EE6B0", ink: "#F7FBFF", muted: "#8AA0B8", card: "#0D1D30", codeBg: "#582E9C", codeInk: "#FFD700" },
  neo: { bgA: "#1A0B2E", bgB: "#582E9C", accent: "#FFD700", ink: "#F7FBFF", muted: "#B7A6D8", card: "#241246", codeBg: "#FFD700", codeInk: "#1A0B2E" },
  amber: { bgA: "#1A0B07", bgB: "#5A2A0A", accent: "#FFC857", ink: "#FFF7E8", muted: "#C9A98F", card: "#2A160B", codeBg: "#5A2A0A", codeInk: "#FFC857" },
  mono: { bgA: "#0A0A0A", bgB: "#1A1A1A", accent: "#FFFFFF", ink: "#FAFAFA", muted: "#A3A3A3", card: "#171717", codeBg: "#FFFFFF", codeInk: "#0A0A0A" },
  editorial: { bgA: "#F4EFE6", bgB: "#E8DFD0", accent: "#A91D1D", ink: "#1A1A1A", muted: "#5C554B", card: "#FFFFFF", codeBg: "#A91D1D", codeInk: "#FFD700", light: true },
  ocean: { bgA: "#061826", bgB: "#0B3B5A", accent: "#7FD7FF", ink: "#F4FBFF", muted: "#93B7C8", card: "#0D2638", codeBg: "#0B3B5A", codeInk: "#7FD7FF" },
  rose: { bgA: "#1A0B14", bgB: "#4A1D2A", accent: "#FF8A65", ink: "#FFF4F0", muted: "#C79A90", card: "#2A111B", codeBg: "#4A1D2A", codeInk: "#FFD700" },
  poster: { bgA: "#F5C518", bgB: "#E0A800", accent: "#07111F", ink: "#07111F", muted: "#4A3C00", card: "#FFFFFF", codeBg: "#07111F", codeInk: "#F5C518", light: true },
};

const clamp = (value: number, min = 0, max = 1) => Math.min(max, Math.max(min, value));

const reveal = (frame: number, fps: number, delay = 0, distance = 34) => {
  const progress = spring({ frame: frame - delay, fps, config: { damping: 18, stiffness: 140 } });
  return {
    opacity: clamp(progress),
    transform: `translateY(${interpolate(progress, [0, 1], [distance, 0])}px)`,
  };
};

const Card: React.FC<{
  label: string;
  value: string;
  palette: Palette;
  frame: number;
  fps: number;
  delay: number;
  width: React.CSSProperties["width"];
}> = ({ label, value, palette, frame, fps, delay, width }) => (
  <div
    style={{
      width,
      minWidth: 0,
      background: palette.card,
      border: `1px solid ${palette.light ? "rgba(0,0,0,.12)" : "rgba(255,255,255,.12)"}`,
      borderRadius: 22,
      padding: 20,
      ...reveal(frame, fps, delay, 28),
    }}
  >
    <div style={{ color: palette.accent, fontSize: 36, fontWeight: 900, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
      {value}
    </div>
    <div style={{ color: palette.muted, fontSize: 14, letterSpacing: 1.4, marginTop: 6, textTransform: "uppercase" }}>
      {label}
    </div>
  </div>
);

export const Promo: React.FC<PromoProps> = (props) => {
  const frame = useCurrentFrame();
  const { fps, width, height, durationInFrames } = useVideoConfig();
  const palette = palettes[props.styleId] ?? palettes.kinetic;
  const portrait = height > width;
  const s1 = Math.floor(durationInFrames * 0.3);
  const s2 = Math.floor(durationInFrames * 0.4);
  const s3 = durationInFrames - s1 - s2;
  const cardWidth = portrait ? "100%" : "31%";
  const code = props.code || "TODAY";
  const sub = props.subheadline || "A limited-time offer.";
  const cta = props.cta || "Tap for details.";

  return (
    <AbsoluteFill style={{ background: palette.bgA, color: palette.ink, fontFamily: "system-ui, -apple-system, sans-serif" }}>
      <AbsoluteFill style={{ background: `radial-gradient(1100px 820px at 78% 8%, ${palette.bgB} 0%, ${palette.bgA} 58%, #030812 100%)` }} />
      {props.image ? (
        <Img
          src={staticFile(props.image)}
          style={{
            position: "absolute",
            inset: 0,
            width: "100%",
            height: "100%",
            objectFit: "cover",
            transform: `scale(${interpolate(frame, [0, durationInFrames], [1.06, 1], { extrapolateRight: "clamp" })})`,
          }}
        />
      ) : null}
      <AbsoluteFill
        style={{
          background: palette.light
            ? "linear-gradient(180deg, rgba(255,255,255,.18), rgba(255,255,255,.55) 46%, rgba(255,255,255,.95))"
            : "linear-gradient(180deg, rgba(3,8,18,.08), rgba(3,8,18,.34) 45%, rgba(3,8,18,.94))",
        }}
      />
      {props.audio ? <Audio src={staticFile(props.audio)} /> : null}

      <Sequence from={0} durationInFrames={s1}>
        <Scene1 frame={frame} fps={fps} palette={palette} portrait={portrait} headline={props.headline} sub={sub} />
      </Sequence>
      <Sequence from={s1} durationInFrames={s2}>
        <Scene2 frame={frame - s1} fps={fps} palette={palette} portrait={portrait} code={code} sub={sub} cta={cta} cardWidth={cardWidth} />
      </Sequence>
      <Sequence from={s1 + s2} durationInFrames={s3}>
        <Scene3 frame={frame - s1 - s2} fps={fps} palette={palette} portrait={portrait} code={code} cta={cta} title={props.title} />
      </Sequence>
    </AbsoluteFill>
  );
};

const Scene1: React.FC<{ frame: number; fps: number; palette: Palette; portrait: boolean; headline: string; sub: string }> = ({ frame, fps, palette, portrait, headline, sub }) => (
  <AbsoluteFill style={{ padding: portrait ? 46 : 64, justifyContent: "center", alignItems: "flex-start" }}>
    <div style={{ ...reveal(frame, fps, 4), color: palette.accent, fontSize: portrait ? 18 : 22, fontWeight: 800, letterSpacing: 3 }}>SPECIAL OFFER</div>
    <div style={{ ...reveal(frame, fps, 10), marginTop: 18, maxWidth: portrait ? "100%" : "88%", fontSize: portrait ? 76 : 96, lineHeight: 0.98, fontWeight: 900, letterSpacing: -3 }}>{headline}</div>
    <div style={{ ...reveal(frame, fps, 22), marginTop: 22, maxWidth: "82%", color: palette.muted, fontSize: portrait ? 26 : 30, lineHeight: 1.35 }}>{sub}</div>
  </AbsoluteFill>
);

const Scene2: React.FC<{ frame: number; fps: number; palette: Palette; portrait: boolean; code: string; sub: string; cta: string; cardWidth: string }> = ({ frame, fps, palette, portrait, code, sub, cta, cardWidth }) => {
  const progress = clamp(interpolate(frame, [12, fps * 3.2], [0, 1], { extrapolateRight: "clamp", easing: Easing.inOut(Easing.ease) }));
  return (
    <AbsoluteFill style={{ padding: portrait ? 46 : 64, justifyContent: "center" }}>
      <div style={{ ...reveal(frame, fps, 2), color: palette.accent, fontSize: portrait ? 18 : 22, fontWeight: 800, letterSpacing: 3 }}>THE OFFER</div>
      <div style={{ ...reveal(frame, fps, 8), marginTop: 12, fontSize: portrait ? 56 : 72, lineHeight: 1, fontWeight: 900, letterSpacing: -2 }}>One offer. <span style={{ color: palette.accent }}>Three reasons.</span></div>
      <div style={{ display: "flex", flexDirection: portrait ? "column" : "row", gap: 14, marginTop: 30 }}>
        <Card label="CODE" value={code} palette={palette} frame={frame} fps={fps} delay={18} width={cardWidth} />
        <Card label="WHY IT MATTERS" value={sub.slice(0, 34)} palette={palette} frame={frame} fps={fps} delay={26} width={cardWidth} />
        <Card label="NEXT STEP" value={cta.slice(0, 24)} palette={palette} frame={frame} fps={fps} delay={34} width={cardWidth} />
      </div>
      <div style={{ marginTop: 28, height: 10, borderRadius: 99, background: palette.light ? "rgba(0,0,0,.12)" : "rgba(255,255,255,.14)", overflow: "hidden" }}>
        <div style={{ width: `${progress * 100}%`, height: "100%", background: palette.accent }} />
      </div>
    </AbsoluteFill>
  );
};

const Scene3: React.FC<{ frame: number; fps: number; palette: Palette; portrait: boolean; code: string; cta: string; title: string }> = ({ frame, fps, palette, portrait, code, cta, title }) => (
  <AbsoluteFill style={{ padding: portrait ? 46 : 64, justifyContent: "center", alignItems: "flex-start" }}>
    <div style={{ ...reveal(frame, fps, 2), color: palette.accent, fontSize: portrait ? 18 : 22, fontWeight: 800, letterSpacing: 3 }}>USE CODE</div>
    <div style={{ ...reveal(frame, fps, 8), marginTop: 16, fontSize: portrait ? 72 : 92, lineHeight: 0.98, fontWeight: 900, letterSpacing: -3 }}>
      Save more.<br /><span style={{ color: palette.accent }}>{code}</span>
    </div>
    <div style={{ ...reveal(frame, fps, 20), marginTop: 24, color: palette.ink, fontSize: portrait ? 24 : 28, fontWeight: 700 }}>↗ {cta}</div>
    <div style={{ ...reveal(frame, fps, 26), marginTop: 18, color: palette.muted, fontSize: 14, letterSpacing: 2, textTransform: "uppercase" }}>{title}</div>
  </AbsoluteFill>
);
