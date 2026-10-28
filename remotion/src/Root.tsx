import { Composition } from "remotion";
import { PwMedEdPulse } from "./PwMedEdPulse";

export const RemotionRoot = () => (
  <Composition
    id="PwMedEdPulse"
    component={PwMedEdPulse}
    durationInFrames={2250}
    fps={30}
    width={1280}
    height={720}
  />
);
