import { Composition } from "remotion";
import { PwMedEdPulse } from "./PwMedEdPulse";
import { P650Promo } from "./P650Promo";
import { PwMedEdP650, p650DurationInFrames } from "./PwMedEdP650";
import { PwMedEdP650Collage, p650CollageDurationInFrames } from "./PwMedEdP650Collage";

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
    <Composition
      id="PwMedEdP650"
      component={PwMedEdP650}
      durationInFrames={p650DurationInFrames}
      fps={30}
      width={1280}
      height={720}
    />
    <Composition
      id="PwMedEdP650Collage"
      component={PwMedEdP650Collage}
      durationInFrames={p650CollageDurationInFrames}
      fps={30}
      width={1280}
      height={720}
    />
  </>
);
