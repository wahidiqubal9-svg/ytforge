import { Composition } from "remotion";
import { PwMedEdPulse } from "./PwMedEdPulse";
import { P650Promo } from "./P650Promo";

export const RemotionRoot = () => (
  <>
    <Composition
      id="PwMedEdPulse"
      component={PwMedEdPulse}
      durationInFrames={2250}
      fps={30}
      width={1280}
      height={720}
    />
    <Composition
      id="P650Promo"
      component={P650Promo}
      durationInFrames={720}
      fps={30}
      width={1280}
      height={720}
    />
  </>
);
