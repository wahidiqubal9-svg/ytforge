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
import { loadFont as loadManrope } from "@remotion/google-fonts/Manrope";
import { loadFont as loadAnton } from "@remotion/google-fonts/Anton";

const { fontFamily: body } = loadManrope("normal", {
  weights: ["400", "600", "800"],
  subsets: ["latin"],
});
const { fontFamily: display } = loadAnton("normal", {
  weights: ["400"],
  subsets: ["latin"],
});

const BG = "#05050A";
const PANEL = "rgba(13,12,24,.82)";
const INK = "#F6F3FF";
const MUTED = "#9A95B8";
const LIME = "#D8FF3E";
const PURPLE = "#9B6BFF";
const VIOLET = "#4C1D95";
const CORAL = "#FF6B6B";

const clamp = (v: number, min = 0, max = 1) => Math.min(max, Math.max(min, v));
const inr = (n: number) => `₹${n.toLocaleString("en-IN")}`;

const enter = (frame: number, fps: number, delay = 0, distance = 30) => {
  const p = spring({ frame: frame - delay, fps, config: { damping: 18, stiffness: 150 } });
  return {
    opacity: clamp(p),
    transform: `translateY(${interpolate(p, [0, 1], [distance, 0])}px)`,
  };
};

const Grain: React.FC = () => (
  <AbsoluteFill
    style={{
      opacity: 0.11,
      backgroundImage: "radial-gradient(rgba(255,255,255,.7) .55px, transparent .55px)",
      backgroundSize: "4px 4px",
      mixBlendMode: "overlay",
      pointerEvents: "none",
    }}
  />
);

const Backdrop: React.FC = () => {
  const frame = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();
  const t = frame / durationInFrames;
  return (
    <AbsoluteFill style={{ background: BG, overflow: "hidden" }}>
      <div
        style={{
          position: "absolute",
          width: 980,
          height: 980,
          borderRadius: "50%",
          left: interpolate(t, [0, 1], [-430, 60]),
          top: interpolate(t, [0, 1], [-500, -140]),
          background: `radial-gradient(circle, ${PURPLE}44 0%, ${VIOLET}18 42%, transparent 70%)`,
          filter: "blur(18px)",
        }}
      />
      <div
        style={{
          position: "absolute",
          width: 820,
          height: 820,
          borderRadius: "50%",
          right: interpolate(t, [0, 1], [-420, 30]),
          bottom: interpolate(t, [0, 1], [-500, -220]),
          background: `radial-gradient(circle, ${LIME}20 0%, transparent 68%)`,
          filter: "blur(26px)",
        }}
      />
      <div
        style={{
          position: "absolute",
          inset: 0,
          backgroundImage:
            "linear-gradient(rgba(155,107,255,.11) 1px, transparent 1px), linear-gradient(90deg, rgba(155,107,255,.11) 1px, transparent 1px)",
          backgroundSize: "68px 68px",
          transform: `translateY(${(frame * 0.22) % 68}px)`,
          maskImage: "radial-gradient(circle at 58% 44%, black 0%, transparent 76%)",
          opacity: 0.8,
        }}
      />
      <div
        style={{
          position: "absolute",
          inset: "-25%",
          background:
            "linear-gradient(112deg, transparent 39%, rgba(216,255,62,.10) 46%, transparent 53%)",
          transform: `translateX(${interpolate(t, [0, 1], [-28, 28])}%)`,
        }}
      />
      <div
        style={{
          position: "absolute",
          inset: 0,
          backgroundImage:
            "repeating-linear-gradient(0deg, rgba(255,255,255,.035) 0px, rgba(255,255,255,.035) 1px, transparent 1px, transparent 5px)",
          opacity: 0.6,
        }}
      />
      <AbsoluteFill
        style={{
          background:
            "radial-gradient(circle at 50% 44%, transparent 28%, rgba(5,5,10,.44) 78%, rgba(5,5,10,.92) 100%)",
        }}
      />
      <Grain />
    </AbsoluteFill>
  );
};

const Sweep: React.FC = () => {
  const frame = useCurrentFrame();
  const x = interpolate(frame, [0, 22], [-30, 130], { extrapolateRight: "clamp" });
  return (
    <div style={{ position: "absolute", inset: 0, overflow: "hidden", pointerEvents: "none" }}>
      <div
        style={{
          position: "absolute",
          left: `${x}%`,
          top: "-30%",
          width: "14%",
          height: "160%",
          transform: "rotate(16deg)",
          background: "linear-gradient(90deg, transparent, rgba(216,255,62,.18), transparent)",
          filter: "blur(4px)",
        }}
      />
    </div>
  );
};

const Stage: React.FC<{ children: React.ReactNode; center?: boolean }> = ({ children, center }) => (
  <AbsoluteFill
    style={{
      padding: "84px 84px 62px",
      justifyContent: "center",
      alignItems: center ? "center" : "flex-start",
      textAlign: center ? "center" : "left",
    }}
  >
    <Sweep />
    {children}
  </AbsoluteFill>
);

const Kicker: React.FC<{ children: React.ReactNode; frame: number; fps: number; delay?: number }> = ({
  children,
  frame,
  fps,
  delay = 0,
}) => (
  <div
    style={{
      ...enter(frame, fps, delay, 18),
      display: "flex",
      alignItems: "center",
      gap: 12,
      color: LIME,
      fontFamily: body,
      fontSize: 12,
      fontWeight: 800,
      letterSpacing: 4,
      textTransform: "uppercase",
    }}
  >
    <span style={{ width: 22, height: 2, background: LIME, display: "inline-block" }} />
    {children}
  </div>
);

const Chip: React.FC<{ children: React.ReactNode; accent?: boolean; style?: React.CSSProperties }> = ({
  children,
  accent,
  style,
}) => (
  <span
    style={{
      display: "inline-flex",
      alignItems: "center",
      gap: 8,
      padding: "9px 15px",
      borderRadius: 4,
      border: `1px solid ${accent ? LIME : "rgba(255,255,255,.18)"}`,
      background: accent ? "rgba(216,255,62,.10)" : "rgba(255,255,255,.04)",
      color: accent ? LIME : INK,
      fontFamily: body,
      fontSize: 11,
      fontWeight: 800,
      letterSpacing: 2.2,
      textTransform: "uppercase",
      ...style,
    }}
  >
    {children}
  </span>
);

const Top: React.FC = () => (
  <div
    style={{
      position: "absolute",
      left: 54,
      right: 54,
      top: 28,
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      zIndex: 20,
    }}
  >
    <div
      style={{
        background: "#FCFCFE",
        borderRadius: 7,
        padding: "7px 12px",
        boxShadow: "0 10px 28px rgba(0,0,0,.34)",
      }}
    >
      <Img src={staticFile("pw-meded-logo.png")} style={{ width: 172, height: "auto", display: "block" }} />
    </div>
    <div style={{ display: "flex", alignItems: "center", gap: 16, fontFamily: body }}>
      <span style={{ color: MUTED, fontSize: 9, fontWeight: 800, letterSpacing: 3 }}>NEET PG · ONE PLAN</span>
      <span style={{ color: LIME, fontSize: 9, fontWeight: 800, letterSpacing: 3 }}>OCTOBER 2026</span>
    </div>
  </div>
);

const Progress: React.FC = () => {
  const frame = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();
  const p = interpolate(frame, [0, durationInFrames], [0, 1], { extrapolateRight: "clamp" });
  return (
    <div style={{ position: "absolute", left: 54, right: 54, bottom: 24, zIndex: 20 }}>
      <div style={{ height: 2, background: "rgba(255,255,255,.12)", overflow: "hidden" }}>
        <div
          style={{
            width: `${p * 100}%`,
            height: "100%",
            background: `linear-gradient(90deg, ${PURPLE}, ${LIME})`,
            boxShadow: `0 0 14px ${LIME}88`,
          }}
        />
      </div>
      <div
        style={{
          marginTop: 7,
          display: "flex",
          justifyContent: "space-between",
          color: "rgba(246,243,255,.38)",
          fontFamily: body,
          fontSize: 8,
          fontWeight: 800,
          letterSpacing: 2,
        }}
      >
        <span>PW MEDED</span>
        <span>P650</span>
      </div>
    </div>
  );
};

const Hero: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  return (
    <Stage>
      <div style={{ position: "absolute", right: 74, top: 130, textAlign: "right" }}>
        <div style={{ ...enter(frame, fps, 8), color: MUTED, fontSize: 9, fontWeight: 800, letterSpacing: 3 }}>
          STARTING AT
        </div>
        <div
          style={{
            ...enter(frame, fps, 12),
            marginTop: 5,
            color: LIME,
            fontFamily: display,
            fontSize: 52,
            lineHeight: 1,
            letterSpacing: 1,
          }}
        >
          ₹5,224
        </div>
      </div>
      <Kicker frame={frame} fps={fps} delay={2}>NEET PG · PREPARATION</Kicker>
      <div
        style={{
          ...enter(frame, fps, 7, 38),
          marginTop: 18,
          color: INK,
          fontFamily: display,
          fontSize: 138,
          lineHeight: 0.82,
          letterSpacing: -1,
        }}
      >
        PREP
        <br />
        <span style={{ color: "transparent", WebkitTextStroke: `2px ${INK}` }}>SMARTER.</span>
      </div>
      <div
        style={{
          ...enter(frame, fps, 15, 30),
          marginTop: 6,
          color: LIME,
          fontFamily: display,
          fontSize: 162,
          lineHeight: 0.82,
          letterSpacing: -1,
          textShadow: `0 0 42px ${LIME}33`,
        }}
      >
        PAY LESS.
      </div>
      <div style={{ ...enter(frame, fps, 23), marginTop: 22, display: "flex", gap: 10 }}>
        <Chip accent>CODE P650</Chip>
        <Chip>5 PLANS · 12–66 MONTHS</Chip>
      </div>
      <div
        style={{
          position: "absolute",
          left: 84,
          bottom: 62,
          color: "rgba(246,243,255,.28)",
          fontFamily: body,
          fontSize: 9,
          fontWeight: 800,
          letterSpacing: 3,
        }}
      >
        01 / 07
      </div>
    </Stage>
  );
};

const Plan: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const ring = interpolate(frame, [0, fps * 5], [0, 300], { extrapolateRight: "clamp" });
  return (
    <Stage>
      <div
        style={{
          position: "absolute",
          right: -70,
          top: -80,
          width: 560,
          height: 560,
          borderRadius: "50%",
          border: `1px solid ${PURPLE}33`,
          boxShadow: `inset 0 0 80px ${PURPLE}12`,
        }}
      />
      <div
        style={{
          position: "absolute",
          right: 22,
          top: 54,
          width: ring,
          height: ring,
          borderRadius: "50%",
          border: `1px solid ${LIME}55`,
          opacity: 0.45,
          transform: "translate(50%, -50%)",
        }}
      />
      <Kicker frame={frame} fps={fps} delay={2}>PW MEDED · ONE PLAN</Kicker>
      <div
        style={{
          ...enter(frame, fps, 8, 34),
          marginTop: 18,
          color: INK,
          fontFamily: display,
          fontSize: 190,
          lineHeight: 0.8,
          letterSpacing: -2,
        }}
      >
        12<span style={{ color: LIME }}>–</span>66
      </div>
      <div
        style={{
          ...enter(frame, fps, 14, 28),
          marginTop: 4,
          color: INK,
          fontFamily: display,
          fontSize: 66,
          lineHeight: 0.9,
          letterSpacing: 2,
        }}
      >
        MONTHS
      </div>
      <div
        style={{
          ...enter(frame, fps, 21),
          marginTop: 18,
          color: MUTED,
          fontFamily: body,
          fontSize: 18,
          lineHeight: 1.5,
          maxWidth: 470,
        }}
      >
        Choose the duration that fits your preparation. Every plan includes the October price.
      </div>
      <div style={{ ...enter(frame, fps, 27), marginTop: 22, display: "flex", gap: 8 }}>
        {["ONE PLAN", "MAX SAVINGS", "FARRE NOTES"].map((label, i) => (
          <Chip key={label} accent={i === 1}>
            {label}
          </Chip>
        ))}
      </div>
      <div
        style={{
          position: "absolute",
          left: 84,
          bottom: 62,
          color: "rgba(246,243,255,.28)",
          fontFamily: body,
          fontSize: 9,
          fontWeight: 800,
          letterSpacing: 3,
        }}
      >
        02 / 07
      </div>
    </Stage>
  );
};

const PRICES = [
  { months: 12, price: 5224, old: 5499 },
  { months: 24, price: 8074, old: 8499 },
  { months: 36, price: 9974, old: 10499 },
  { months: 48, price: 12349, old: 12999 },
  { months: 66, price: 14249, old: 14999 },
];

const PriceScene: React.FC<{ plan: typeof PRICES[number]; index: number }> = ({ plan, index }) => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();
  const p = spring({ frame, fps, config: { damping: 16, stiffness: 150 } });
  const count = clamp(interpolate(frame, [8, 42], [0, 1], { extrapolateRight: "clamp" }));
  const shown = Math.round(interpolate(Easing.out(Easing.cubic)(count), [0, 1], [0, plan.price]));
  const save = plan.old - plan.price;
  const out = interpolate(frame, [durationInFrames - 10, durationInFrames], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  return (
    <Stage center>
      <div
        style={{
          position: "absolute",
          color: "rgba(255,255,255,.035)",
          fontFamily: display,
          fontSize: 520,
          lineHeight: 0.7,
          letterSpacing: -10,
          transform: `translateY(${interpolate(frame, [0, 168], [18, -18])}px)`,
        }}
      >
        {plan.months}
      </div>
      <div style={{ ...enter(frame, fps, 1), color: LIME, fontFamily: body, fontSize: 11, fontWeight: 800, letterSpacing: 4 }}>
        OCTOBER PRICING · {String(index + 1).padStart(2, "0")} / 05
      </div>
      <div
        style={{
          marginTop: 18,
          width: 760,
          padding: "30px 34px 28px",
          borderRadius: 10,
          border: "1px solid rgba(255,255,255,.16)",
          background: `linear-gradient(145deg, rgba(24,20,44,.96), ${PANEL})`,
          boxShadow: `0 30px 90px rgba(76,29,149,.36), inset 0 1px 0 rgba(255,255,255,.12)`,
          transform: `perspective(1000px) rotateY(${interpolate(p, [0, 1], [-15, 0])}deg) rotateX(${interpolate(p, [0, 1], [7, 0])}deg) translateY(${interpolate(p, [0, 1], [40, 0])}px) scale(${interpolate(p, [0, 1], [.94, 1])})`,
          opacity: clamp(p) * out,
          overflow: "hidden",
        }}
      >
        <div
          style={{
            position: "absolute",
            inset: 0,
            background: `linear-gradient(115deg, transparent 25%, rgba(216,255,62,.10) 46%, transparent 63%)`,
            transform: `translateX(${interpolate(frame, [0, 80], [-70, 70])}%)`,
          }}
        />
        <div style={{ position: "relative", display: "flex", alignItems: "flex-start", justifyContent: "space-between" }}>
          <div>
            <div style={{ color: MUTED, fontFamily: body, fontSize: 11, fontWeight: 800, letterSpacing: 3 }}>PW MEDED ONE PLAN</div>
            <div style={{ marginTop: 8, color: INK, fontFamily: display, fontSize: 76, lineHeight: 0.9, letterSpacing: 1 }}>
              {plan.months} <span style={{ color: LIME }}>MONTHS</span>
            </div>
          </div>
          <div
            style={{
              border: `1px solid ${LIME}66`,
              background: "rgba(216,255,62,.08)",
              color: LIME,
              padding: "8px 11px",
              fontFamily: body,
              fontSize: 10,
              fontWeight: 800,
              letterSpacing: 1.4,
            }}
          >
            SAVE {inr(save)}
          </div>
        </div>
        <div style={{ position: "relative", display: "flex", alignItems: "baseline", gap: 16, marginTop: 22 }}>
          <div style={{ color: LIME, fontFamily: display, fontSize: 104, lineHeight: 0.85, letterSpacing: 1 }}>
            {inr(shown)}
          </div>
          <div style={{ color: "#77718F", fontFamily: body, fontSize: 19, fontWeight: 600, textDecoration: "line-through" }}>
            {inr(plan.old)}
          </div>
        </div>
        <div style={{ position: "relative", marginTop: 20, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div style={{ color: MUTED, fontFamily: body, fontSize: 10, fontWeight: 800, letterSpacing: 2 }}>ONE PLAN · OCTOBER PRICE</div>
          <div style={{ display: "flex", gap: 4 }}>
            {PRICES.map((_, i) => (
              <div
                key={i}
                style={{
                  width: i === index ? 26 : 7,
                  height: 3,
                  background: i === index ? LIME : "rgba(255,255,255,.18)",
                }}
              />
            ))}
          </div>
        </div>
      </div>
      <div style={{ ...enter(frame, fps, 4), marginTop: 22, color: MUTED, fontFamily: body, fontSize: 12, letterSpacing: 2 }}>
        LIMITED-TIME OCTOBER PRICE
      </div>
    </Stage>
  );
};

const Bonus: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const book = spring({ frame: frame - 10, fps, config: { damping: 17, stiffness: 120 } });
  return (
    <Stage>
      <Kicker frame={frame} fps={fps} delay={2}>BONUS · INCLUDED</Kicker>
      <div
        style={{
          ...enter(frame, fps, 8, 32),
          marginTop: 14,
          color: INK,
          fontFamily: display,
          fontSize: 104,
          lineHeight: 0.84,
          letterSpacing: 0,
        }}
      >
        FARRE
        <br />
        <span style={{ color: "transparent", WebkitTextStroke: `2px ${INK}` }}>NOTES</span>
      </div>
      <div
        style={{
          ...enter(frame, fps, 15),
          marginTop: 4,
          color: LIME,
          fontFamily: display,
          fontSize: 140,
          lineHeight: 0.9,
        }}
      >
        FREE.
      </div>
      <div style={{ ...enter(frame, fps, 22), marginTop: 10, color: MUTED, fontFamily: body, fontSize: 18 }}>
        On 24+ month plans.
      </div>
      <div style={{ ...enter(frame, fps, 28), marginTop: 22, display: "flex", gap: 8 }}>
        <Chip accent>REVISION READY</Chip>
        <Chip>24+ MONTHS</Chip>
      </div>
      <div
        style={{
          position: "absolute",
          right: 126,
          top: 144,
          width: 250,
          height: 330,
          transform: `perspective(900px) rotateY(${interpolate(book, [0, 1], [-28, -16])}deg) rotateX(${interpolate(book, [0, 1], [12, 5])}deg) translateY(${interpolate(book, [0, 1], [34, 0])}px)`,
          opacity: clamp(book),
          borderRadius: "6px 14px 14px 6px",
          background: `linear-gradient(145deg, #1C1738, #090812 72%)`,
          borderLeft: `14px solid #05040B`,
          boxShadow: `-28px 30px 60px rgba(0,0,0,.52), 0 0 50px ${PURPLE}22`,
          overflow: "hidden",
        }}
      >
        <div
          style={{
            position: "absolute",
            inset: 0,
            background: `linear-gradient(112deg, transparent 20%, rgba(216,255,62,.16) 47%, transparent 64%)`,
            transform: `translateX(${interpolate(frame, [10, 90], [-80, 80])}%)`,
          }}
        />
        <div style={{ padding: "28px 24px" }}>
          <div style={{ color: PURPLE, fontFamily: body, fontSize: 9, fontWeight: 800, letterSpacing: 3 }}>PW MEDED</div>
          <div style={{ marginTop: 44, color: INK, fontFamily: display, fontSize: 58, lineHeight: 0.85, letterSpacing: 1 }}>FARRE</div>
          <div style={{ marginTop: 8, color: LIME, fontFamily: body, fontSize: 10, fontWeight: 800, letterSpacing: 2 }}>QUICK REVISION</div>
          <div style={{ marginTop: 74, width: 54, height: 2, background: LIME }} />
          <div style={{ marginTop: 10, color: MUTED, fontFamily: body, fontSize: 9, letterSpacing: 2 }}>NEET PG · 2026</div>
        </div>
      </div>
      <div
        style={{
          position: "absolute",
          right: 88,
          top: 116,
          width: 94,
          height: 94,
          borderRadius: "50%",
          background: LIME,
          color: BG,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          textAlign: "center",
          fontFamily: body,
          fontSize: 11,
          fontWeight: 800,
          letterSpacing: 1,
          transform: `rotate(${interpolate(frame, [0, 180], [-12, 8])}deg) scale(${clamp(spring({ frame: frame - 24, fps, config: { damping: 12, stiffness: 180 } }))})`,
          boxShadow: `0 0 40px ${LIME}55`,
        }}
      >
        FREE
        <br />
        BONUS
      </div>
    </Stage>
  );
};

const Coupon: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame: frame - 6, fps, config: { damping: 13, stiffness: 170 } });
  return (
    <Stage center>
      <div
        style={{
          position: "absolute",
          inset: 0,
          backgroundImage:
            "linear-gradient(rgba(216,255,62,.10) 1px, transparent 1px), linear-gradient(90deg, rgba(216,255,62,.10) 1px, transparent 1px)",
          backgroundSize: "54px 54px",
          transform: "perspective(500px) rotateX(52deg) translateY(210px)",
          transformOrigin: "bottom",
          maskImage: "linear-gradient(transparent 18%, black 80%)",
        }}
      />
      <Kicker frame={frame} fps={fps} delay={1}>EXTRA SAVINGS</Kicker>
      <div style={{ ...enter(frame, fps, 7), marginTop: 10, color: INK, fontFamily: body, fontSize: 22, fontWeight: 800, letterSpacing: 1 }}>
        USE CODE
      </div>
      <div
        style={{
          marginTop: 14,
          padding: "4px 34px",
          border: `2px dashed ${LIME}`,
          background: "rgba(216,255,62,.06)",
          boxShadow: `0 0 60px ${LIME}22`,
          transform: `perspective(800px) rotateX(${interpolate(p, [0, 1], [14, 0])}deg) scale(${interpolate(p, [0, 1], [.8, 1])})`,
          opacity: clamp(p),
        }}
      >
        <div style={{ color: LIME, fontFamily: display, fontSize: 228, lineHeight: 1, letterSpacing: 8 }}>P650</div>
      </div>
      <div style={{ ...enter(frame, fps, 18), marginTop: 14, color: MUTED, fontFamily: body, fontSize: 14, letterSpacing: 2 }}>
        APPLY AT CHECKOUT
      </div>
      <div style={{ ...enter(frame, fps, 24), marginTop: 20, display: "flex", gap: 8 }}>
        <Chip accent>EXTRA DISCOUNT</Chip>
        <Chip>ONE PLAN</Chip>
      </div>
    </Stage>
  );
};

const STEP_DATA = [
  ["01", "CHOOSE YOUR PLAN", "12–66 months"],
  ["02", "OPEN CHECKOUT", "PW MedEd"],
  ["03", "ENTER P650", "Coupon code"],
  ["04", "APPLY & SAVE", "October price"],
];

const Steps: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const line = interpolate(frame, [18, 148], [0, 1], { extrapolateRight: "clamp" });
  return (
    <Stage>
      <Kicker frame={frame} fps={fps} delay={2}>HOW TO APPLY</Kicker>
      <div
        style={{
          ...enter(frame, fps, 7, 30),
          marginTop: 10,
          color: INK,
          fontFamily: display,
          fontSize: 82,
          lineHeight: 0.9,
          letterSpacing: 0,
        }}
      >
        4 STEPS. <span style={{ color: LIME }}>SAVE MORE.</span>
      </div>
      <div style={{ position: "relative", marginTop: 26, width: 760 }}>
        <div style={{ position: "absolute", left: 17, top: 12, bottom: 12, width: 2, background: "rgba(255,255,255,.12)" }} />
        <div
          style={{
            position: "absolute",
            left: 17,
            top: 12,
            width: 2,
            height: `${line * 100}%`,
            background: LIME,
            boxShadow: `0 0 12px ${LIME}88`,
          }}
        />
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {STEP_DATA.map(([n, title, sub], i) => (
            <div
              key={n}
              style={{
                ...enter(frame, fps, 18 + i * 22, 18),
                display: "flex",
                alignItems: "center",
                gap: 18,
                background: "rgba(255,255,255,.035)",
                border: "1px solid rgba(255,255,255,.10)",
                padding: "12px 16px",
              }}
            >
              <div
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: "50%",
                  background: i === 2 ? LIME : "rgba(155,107,255,.18)",
                  border: `1px solid ${i === 2 ? LIME : "rgba(155,107,255,.45)"}`,
                  color: i === 2 ? BG : PURPLE,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontFamily: body,
                  fontSize: 10,
                  fontWeight: 800,
                }}
              >
                {n}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ color: INK, fontFamily: body, fontSize: 15, fontWeight: 800, letterSpacing: 1.2 }}>{title}</div>
                <div style={{ color: MUTED, fontFamily: body, fontSize: 10, letterSpacing: 1.4, marginTop: 4 }}>{sub}</div>
              </div>
              {i === 2 ? <Chip accent>P650</Chip> : null}
            </div>
          ))}
        </div>
      </div>
      <div
        style={{
          position: "absolute",
          right: 84,
          top: 178,
          color: "rgba(216,255,62,.10)",
          fontFamily: display,
          fontSize: 280,
          lineHeight: 0.8,
        }}
      >
        4×
      </div>
    </Stage>
  );
};

const Cta: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  return (
    <Stage center>
      <div
        style={{
          position: "absolute",
          width: 540,
          height: 540,
          borderRadius: "50%",
          border: `1px solid ${LIME}22`,
          boxShadow: `0 0 100px ${LIME}12, inset 0 0 90px ${PURPLE}18`,
        }}
      />
      <Kicker frame={frame} fps={fps} delay={1}>GET THE DEAL</Kicker>
      <div
        style={{
          ...enter(frame, fps, 7, 34),
          marginTop: 18,
          color: INK,
          fontFamily: display,
          fontSize: 86,
          lineHeight: 0.9,
          letterSpacing: 0,
        }}
      >
        MEDICAL
        <br />
        <span style={{ color: LIME }}>COUPONS.IN</span>
      </div>
      <div style={{ ...enter(frame, fps, 15), marginTop: 14, color: MUTED, fontFamily: body, fontSize: 16, letterSpacing: 2 }}>
        ALL OFFERS. ONE PLACE.
      </div>
      <div style={{ ...enter(frame, fps, 22), marginTop: 22, display: "flex", gap: 10 }}>
        <Chip accent style={{ fontSize: 15, padding: "12px 20px" }}>CODE P650</Chip>
        <Chip style={{ fontSize: 15, padding: "12px 20px" }}>OCTOBER 2026</Chip>
      </div>
      <div
        style={{
          ...enter(frame, fps, 30),
          marginTop: 30,
          display: "flex",
          alignItems: "center",
          gap: 14,
          color: "rgba(246,243,255,.42)",
          fontFamily: body,
          fontSize: 9,
          fontWeight: 800,
          letterSpacing: 3,
        }}
      >
        <span style={{ width: 34, height: 1, background: "rgba(246,243,255,.32)" }} />
        PREP SMARTER. PAY LESS.
        <span style={{ width: 34, height: 1, background: "rgba(246,243,255,.32)" }} />
      </div>
    </Stage>
  );
};

const cuts = {
  hero: 0,
  plan: 8,
  price: 15.5,
  bonus: 43.5,
  coupon: 50.2,
  steps: 56.1,
  cta: 68.3,
  end: 75,
};

const priceDuration = (cuts.bonus - cuts.price) / PRICES.length;

export const PwMedEdPulse: React.FC = () => {
  const { fps } = useVideoConfig();
  const s = (seconds: number) => Math.max(1, Math.floor(seconds * fps));
  return (
    <AbsoluteFill style={{ background: BG, color: INK, fontFamily: `${body}, system-ui, sans-serif` }}>
      <Backdrop />
      <Top />
      <Sequence from={s(cuts.hero)} durationInFrames={s(cuts.plan - cuts.hero)}>
        <Hero />
      </Sequence>
      <Sequence from={s(cuts.plan)} durationInFrames={s(cuts.price - cuts.plan)}>
        <Plan />
      </Sequence>
      {PRICES.map((plan, index) => (
        <Sequence
          key={plan.months}
          from={s(cuts.price + index * priceDuration)}
          durationInFrames={s(priceDuration)}
        >
          <PriceScene plan={plan} index={index} />
          <Audio src={staticFile("sfx/pop.wav")} volume={0.55} />
        </Sequence>
      ))}
      <Sequence from={s(cuts.bonus)} durationInFrames={s(cuts.coupon - cuts.bonus)}>
        <Bonus />
        <Audio src={staticFile("sfx/sparkle.wav")} volume={0.55} />
      </Sequence>
      <Sequence from={s(cuts.coupon)} durationInFrames={s(cuts.steps - cuts.coupon)}>
        <Coupon />
        <Audio src={staticFile("sfx/ding.wav")} volume={0.6} />
      </Sequence>
      <Sequence from={s(cuts.steps)} durationInFrames={s(cuts.cta - cuts.steps)}>
        <Steps />
        <Audio src={staticFile("sfx/click.wav")} volume={0.45} />
      </Sequence>
      <Sequence from={s(cuts.cta)} durationInFrames={s(cuts.end - cuts.cta)}>
        <Cta />
        <Audio src={staticFile("sfx/whoosh.wav")} volume={0.55} />
      </Sequence>
      <Audio src={staticFile("sfx/whoosh.wav")} volume={0.45} />
      <Progress />
      <Audio src={staticFile("voice-pw.mp3")} />
    </AbsoluteFill>
  );
};
