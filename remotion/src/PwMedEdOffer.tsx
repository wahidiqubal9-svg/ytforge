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
import { loadFont } from "@remotion/google-fonts/Manrope";

const { fontFamily } = loadFont("normal", { weights: ["400", "600", "800"], subsets: ["latin"] });

const NAVY = "#0B1133";
const PURPLE = "#6C2BD9";
const LAVENDER = "#F1ECFF";
const MUTED = "#6F6A8A";
const WHITE = "#FFFFFF";

const clamp = (v: number, min = 0, max = 1) => Math.min(max, Math.max(min, v));

const enter = (frame: number, fps: number, delay = 0, distance = 24) => {
  const p = spring({ frame: frame - delay, fps, config: { damping: 18, stiffness: 150 } });
  return {
    opacity: clamp(p),
    transform: `translateY(${interpolate(p, [0, 1], [distance, 0])}px)`,
  };
};

const Bg: React.FC = () => {
  const frame = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();
  const t = frame / durationInFrames;
  return (
    <AbsoluteFill style={{ background: "#FBFAFF", overflow: "hidden" }}>
      <div style={{ position: "absolute", width: 520, height: 520, borderRadius: "50%", left: -180, top: -220, background: "radial-gradient(circle, #DCCEFF88 0%, transparent 68%)" }} />
      <div style={{ position: "absolute", width: 420, height: 420, borderRadius: "50%", right: -160 + Math.sin(t * Math.PI * 2) * 30, bottom: -180, background: "radial-gradient(circle, #FFF0B866 0%, transparent 68%)" }} />
      <div style={{ position: "absolute", inset: 0, backgroundImage: "linear-gradient(rgba(108,43,217,.055) 1px, transparent 1px), linear-gradient(90deg, rgba(108,43,217,.055) 1px, transparent 1px)", backgroundSize: "40px 40px", maskImage: "radial-gradient(circle at 60% 40%, black 0%, transparent 78%)" }} />
      <div style={{ position: "absolute", inset: 0, background: "linear-gradient(180deg, rgba(255,255,255,.4), rgba(255,255,255,.94))" }} />
    </AbsoluteFill>
  );
};

const Top: React.FC = () => (
  <div style={{ position: "absolute", left: 26, right: 26, top: 18, display: "flex", alignItems: "center", justifyContent: "space-between", zIndex: 10 }}>
    <Img src={staticFile("pw-meded-logo.png")} style={{ width: 148, height: "auto", mixBlendMode: "multiply" }} />
    <div style={{ color: PURPLE, fontSize: 8, fontWeight: 800, letterSpacing: 2, textTransform: "uppercase" }}>October Offer · 2026</div>
  </div>
);

const Progress: React.FC<{ progress: number }> = ({ progress }) => (
  <div style={{ position: "absolute", left: 26, right: 26, bottom: 12, height: 3, borderRadius: 99, background: "#E5DFF7" }}>
    <div style={{ width: `${progress * 100}%`, height: "100%", borderRadius: 99, background: `linear-gradient(90deg, ${PURPLE}, #B48CFF)` }} />
  </div>
);

const Scene: React.FC<{ children: React.ReactNode; center?: boolean }> = ({ children, center }) => (
  <AbsoluteFill style={{ padding: "56px 30px 30px", justifyContent: "center", alignItems: center ? "center" : "flex-start", textAlign: center ? "center" : "left" }}>
    {children}
  </AbsoluteFill>
);

const Hero: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  return (
    <Scene>
      <div style={{ ...enter(frame, fps, 2), color: PURPLE, fontSize: 9, fontWeight: 800, letterSpacing: 2.6, textTransform: "uppercase" }}>NEET PG · PW MEDED</div>
      <div style={{ ...enter(frame, fps, 7), marginTop: 8, color: NAVY, fontSize: 47, lineHeight: 0.96, fontWeight: 800, letterSpacing: -2.4 }}>
        PREP SMARTER.<br /><span style={{ color: PURPLE }}>PAY LESS.</span>
      </div>
      <div style={{ ...enter(frame, fps, 15), marginTop: 10, color: MUTED, fontSize: 14, lineHeight: 1.4 }}>PW MedEd One Plan · October offer</div>
      <div style={{ ...enter(frame, fps, 22), marginTop: 14, display: "flex", gap: 8 }}>
        <Pill>12–66 MONTHS</Pill>
        <Pill accent>CODE P650</Pill>
      </div>
    </Scene>
  );
};

const Pill: React.FC<{ children: React.ReactNode; accent?: boolean }> = ({ children, accent }) => (
  <span style={{
    display: "inline-flex", alignItems: "center",
    color: accent ? WHITE : PURPLE,
    background: accent ? PURPLE : LAVENDER,
    border: `1px solid ${accent ? PURPLE : "#DCCEFF"}`,
    borderRadius: 99, padding: "5px 10px", fontSize: 9, fontWeight: 800, letterSpacing: 1.2,
  }}>{children}</span>
);

const PlanIntro: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  return (
    <Scene center>
      <div style={{ ...enter(frame, fps, 2), color: PURPLE, fontSize: 9, fontWeight: 800, letterSpacing: 2.6 }}>PW MEDED ONE PLAN</div>
      <div style={{ ...enter(frame, fps, 7), marginTop: 8, color: NAVY, fontSize: 38, fontWeight: 800, letterSpacing: -1.8 }}>12–66 MONTHS</div>
      <div style={{ ...enter(frame, fps, 14), marginTop: 8, color: MUTED, fontSize: 13, maxWidth: 360, lineHeight: 1.4 }}>Choose the duration that fits your preparation.</div>
    </Scene>
  );
};

const PRICES = [
  { months: 12, price: 5224, old: 5499 },
  { months: 24, price: 8074, old: 8499 },
  { months: 36, price: 9974, old: 10499 },
  { months: 48, price: 12349, old: 12999 },
  { months: 66, price: 14249, old: 14999 },
];

const inr = (n: number) => `₹${n.toLocaleString("en-IN")}`;

const PriceCard: React.FC<{ plan: typeof PRICES[number] }> = ({ plan }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame, fps, config: { damping: 16, stiffness: 150 } });
  const shown = Math.round(interpolate(p, [0, 1], [0, plan.price]));
  const save = plan.old - plan.price;
  return (
    <div style={{
      width: 330, background: WHITE, borderRadius: 18, padding: "18px 20px",
      border: "1px solid #E4DDF8", boxShadow: "0 18px 38px rgba(76,38,160,.13)",
      transform: `translateY(${interpolate(p, [0, 1], [18, 0])}px) scale(${interpolate(p, [0, 1], [.97, 1])})`,
      opacity: clamp(p),
    }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ color: NAVY, fontSize: 17, fontWeight: 800, letterSpacing: -0.4 }}>{plan.months} MONTHS</div>
        <div style={{ color: PURPLE, fontSize: 8, fontWeight: 800, letterSpacing: 1.4, background: LAVENDER, padding: "4px 7px", borderRadius: 99 }}>SAVE {inr(save)}</div>
      </div>
      <div style={{ display: "flex", alignItems: "baseline", gap: 9, marginTop: 10 }}>
        <div style={{ color: PURPLE, fontSize: 34, lineHeight: 1, fontWeight: 800, letterSpacing: -1.2 }}>{inr(shown)}</div>
        <div style={{ color: "#A09BB5", fontSize: 12, textDecoration: "line-through", fontWeight: 600 }}>{inr(plan.old)}</div>
      </div>
      <div style={{ color: MUTED, fontSize: 8, letterSpacing: 1.2, marginTop: 8, textTransform: "uppercase" }}>One Plan · October price</div>
    </div>
  );
};

const Prices: React.FC<{ start: number }> = ({ start }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const index = Math.min(PRICES.length - 1, Math.floor((frame - start) / (fps * 5.4)));
  return (
    <Scene center>
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 12 }}>
        <div style={{ color: PURPLE, fontSize: 9, fontWeight: 800, letterSpacing: 2.4 }}>OCTOBER PRICING</div>
        <PriceCard plan={PRICES[Math.max(0, index)]} />
        <div style={{ display: "flex", gap: 4 }}>
          {PRICES.map((_, i) => (
            <div key={i} style={{ width: i === index ? 16 : 5, height: 5, borderRadius: 99, background: i === index ? PURPLE : "#DCD3F2" }} />
          ))}
        </div>
      </div>
    </Scene>
  );
};

const Fare: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  return (
    <Scene>
      <div style={{ ...enter(frame, fps, 2), color: PURPLE, fontSize: 9, fontWeight: 800, letterSpacing: 2.4 }}>BONUS</div>
      <div style={{ ...enter(frame, fps, 7), marginTop: 6, color: NAVY, fontSize: 38, lineHeight: 1, fontWeight: 800, letterSpacing: -1.8 }}>FARRE NOTES<br /><span style={{ color: PURPLE }}>FREE.</span></div>
      <div style={{ ...enter(frame, fps, 15), marginTop: 12, color: MUTED, fontSize: 13 }}>On 24+ month plans.</div>
      <div style={{ ...enter(frame, fps, 22), marginTop: 16, width: 112, height: 138, borderRadius: "5px 12px 12px 5px", background: `linear-gradient(145deg, ${NAVY}, #1D1746)`, borderLeft: "7px solid #070A20", boxShadow: `-10px 14px 24px rgba(44,20,100,.25)`, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
        <div style={{ color: "#B9A7FF", fontSize: 7, letterSpacing: 1.6 }}>PW MEDED</div>
        <div style={{ color: WHITE, fontFamily: "Georgia, serif", fontSize: 20, letterSpacing: 1 }}>FARRE</div>
        <div style={{ color: "#B9A7FF", fontSize: 6, letterSpacing: 1 }}>QUICK REVISION</div>
      </div>
    </Scene>
  );
};

const Coupon: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame, fps, config: { damping: 13, stiffness: 170 } });
  return (
    <Scene center>
      <div style={{ ...enter(frame, fps, 2), color: PURPLE, fontSize: 9, fontWeight: 800, letterSpacing: 2.4 }}>EXTRA SAVINGS</div>
      <div style={{ ...enter(frame, fps, 7), marginTop: 6, color: NAVY, fontSize: 18, fontWeight: 800 }}>USE CODE</div>
      <div style={{
        marginTop: 8, padding: "8px 24px", border: `2px dashed ${PURPLE}`, borderRadius: 16,
        color: PURPLE, fontSize: 62, lineHeight: 1, fontWeight: 800, letterSpacing: 4,
        transform: `scale(${interpolate(p, [0, 1], [.82, 1])})`, opacity: clamp(p),
        background: "#FAF7FF", boxShadow: "0 14px 40px rgba(108,43,217,.14)",
      }}>P650</div>
      <div style={{ ...enter(frame, fps, 18), marginTop: 10, color: MUTED, fontSize: 11 }}>Apply at checkout.</div>
    </Scene>
  );
};

const Steps: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  return (
    <Scene>
      <div style={{ ...enter(frame, fps, 2), color: PURPLE, fontSize: 9, fontWeight: 800, letterSpacing: 2.4 }}>HOW TO APPLY</div>
      <div style={{ ...enter(frame, fps, 7), marginTop: 6, color: NAVY, fontSize: 31, fontWeight: 800, letterSpacing: -1.2 }}>4 quick steps.</div>
      <div style={{ display: "flex", flexDirection: "column", gap: 6, marginTop: 14, width: "100%", maxWidth: 390 }}>
        {["Choose your plan", "Open checkout", "Enter code P650", "Apply and save"].map((text, i) => (
          <div key={text} style={{ ...enter(frame, fps, 13 + i * 7), display: "flex", alignItems: "center", gap: 10, background: WHITE, border: "1px solid #E7E0F7", borderRadius: 9, padding: "7px 10px", boxShadow: "0 7px 18px rgba(76,38,160,.06)" }}>
            <div style={{ color: PURPLE, fontSize: 9, fontWeight: 800 }}>0{i + 1}</div>
            <div style={{ color: NAVY, fontSize: 11, fontWeight: 700 }}>{text}</div>
          </div>
        ))}
      </div>
    </Scene>
  );
};

const Cta: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  return (
    <Scene center>
      <div style={{ ...enter(frame, fps, 2), color: PURPLE, fontSize: 9, fontWeight: 800, letterSpacing: 2.4 }}>GET THE DEAL</div>
      <div style={{ ...enter(frame, fps, 8), marginTop: 8, color: NAVY, fontSize: 31, fontWeight: 800, letterSpacing: -1.2 }}>medicalcoupons.in</div>
      <div style={{ ...enter(frame, fps, 16), marginTop: 8, color: MUTED, fontSize: 11 }}>All offers. One place.</div>
      <div style={{ ...enter(frame, fps, 22), marginTop: 14, background: PURPLE, color: WHITE, borderRadius: 99, padding: "8px 16px", fontSize: 10, fontWeight: 800, letterSpacing: 1.2 }}>CODE: P650</div>
    </Scene>
  );
};

export const PwMedEdOffer: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();
  const progress = interpolate(frame, [0, durationInFrames], [0, 1], { extrapolateRight: "clamp" });
  const s = (v: number) => Math.floor(v * fps);
  return (
    <AbsoluteFill style={{ fontFamily: `${fontFamily}, system-ui, sans-serif`, color: NAVY }}>
      <Bg />
      <Top />
      <Sequence from={0} durationInFrames={s(8)}>
        <Hero />
        <Audio src={staticFile("sfx/whoosh.wav")} volume={0.7} />
      </Sequence>
      <Sequence from={s(8)} durationInFrames={s(8)}>
        <PlanIntro />
        <Audio src={staticFile("sfx/whoosh.wav")} volume={0.6} />
      </Sequence>
      {PRICES.map((plan, index) => {
        const from = 16 + index * 5.4;
        return (
          <Sequence key={plan.months} from={s(from)} durationInFrames={s(5.4)}>
            <Scene center><PriceCard plan={plan} /></Scene>
            <Audio src={staticFile("sfx/pop.wav")} volume={0.8} />
          </Sequence>
        );
      })}
      <Sequence from={s(44)} durationInFrames={s(9)}>
        <Fare />
        <Audio src={staticFile("sfx/sparkle.wav")} volume={0.7} />
      </Sequence>
      <Sequence from={s(53)} durationInFrames={s(8)}>
        <Coupon />
        <Audio src={staticFile("sfx/ding.wav")} volume={0.8} />
      </Sequence>
      <Sequence from={s(61)} durationInFrames={s(7)}>
        <Steps />
        <Audio src={staticFile("sfx/click.wav")} volume={0.55} />
      </Sequence>
      <Sequence from={s(68)} durationInFrames={s(7)}>
        <Cta />
        <Audio src={staticFile("sfx/whoosh.wav")} volume={0.6} />
      </Sequence>
      <Progress progress={progress} />
      <Audio src={staticFile("voice-pw.mp3")} />
    </AbsoluteFill>
  );
};
