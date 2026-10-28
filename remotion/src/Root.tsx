import { Composition, type CalculateMetadataFunction } from "remotion";
import { Promo, type PromoProps } from "./Promo";

const defaultProps: PromoProps = {
  headline: "Make videos by chatting.",
  subheadline: "Script, voice, render and publish from one place.",
  code: "P650",
  cta: "medicalcoupons.in",
  title: "YTForge",
  styleId: "kinetic",
  ratio: "16:9",
  durationInFrames: 300,
};

const duration: CalculateMetadataFunction<PromoProps> = ({ props }) => ({
  durationInFrames: Math.max(30, props.durationInFrames ?? 300),
  fps: 30,
});

export const RemotionRoot = () => (
  <>
    <Composition
      id="Promo"
      component={Promo}
      durationInFrames={300}
      fps={30}
      width={1280}
      height={720}
      defaultProps={defaultProps}
      calculateMetadata={duration}
    />
    <Composition
      id="PromoPortrait"
      component={Promo}
      durationInFrames={300}
      fps={30}
      width={720}
      height={1280}
      defaultProps={{ ...defaultProps, ratio: "9:16" }}
      calculateMetadata={duration}
    />
    <Composition
      id="PromoSquare"
      component={Promo}
      durationInFrames={300}
      fps={30}
      width={720}
      height={720}
      defaultProps={{ ...defaultProps, ratio: "1:1" }}
      calculateMetadata={duration}
    />
  </>
);
