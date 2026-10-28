import React from "react";
import {
  AbsoluteFill, Composition, Easing, interpolate, spring,
  useCurrentFrame, useVideoConfig,
} from "remotion";

const BG = "#070914";
const CYAN = "#65F5FF";
const VIOLET = "#A78BFA";
const WHITE = "#F7F8FF";
const MUTED = "#A7B0CC";
const clamp = (v: number) => Math.max(0, Math.min(1, v));

const Backdrop: React.FC = () => {
  const f = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();
  const p = f / durationInFrames;
  return <AbsoluteFill style={{ background: BG, overflow: "hidden" }}>
    <div style={{position:"absolute",inset:-180,background:
      `radial-gradient(circle at ${35+p*30}% ${30+Math.sin(f/40)*8}%, #4F46E566, transparent 34%),
       radial-gradient(circle at ${75-p*20}% 80%, #06B6D433, transparent 32%)`}} />
    <div style={{position:"absolute",inset:0,opacity:.24,backgroundImage:
      "linear-gradient(rgba(101,245,255,.18) 1px,transparent 1px),linear-gradient(90deg,rgba(101,245,255,.18) 1px,transparent 1px)",
      backgroundSize:"64px 64px",transform:`perspective(600px) rotateX(18deg) translateY(${(f*0.45)%64}px)`,
      transformOrigin:"center 65%"}} />
    {Array.from({length:22},(_,i)=><div key={i} style={{
      position:"absolute",left:`${(i*47+f*(.25+i%3*.08))%110-5}%`,
      top:`${(i*71+Math.sin(f/26+i)*18)%100}%`,width:i%4===0?5:2,height:i%4===0?5:2,
      borderRadius:99,background:i%2?CYAN:VIOLET,opacity:.3+(i%4)*.12,
      boxShadow:`0 0 14px ${i%2?CYAN:VIOLET}`}} />)}
    <AbsoluteFill style={{background:"radial-gradient(ellipse at center, transparent 35%, #050610CC 100%)"}}/>
  </AbsoluteFill>;
};

const Eyebrow: React.FC<{text:string; frame:number}> = ({text,frame}) => {
  const {fps}=useVideoConfig();
  const p=spring({frame:frame-4,fps,config:{damping:16,stiffness:130}});
  return <div style={{opacity:clamp(p),transform:`translateY(${interpolate(p,[0,1],[16,0])}px)`,
    color:CYAN,fontSize:15,fontWeight:800,letterSpacing:5,textTransform:"uppercase"}}>{text}</div>;
};

const Brand: React.FC = () => <div style={{position:"absolute",top:34,left:54,right:54,
  display:"flex",justifyContent:"space-between",alignItems:"center",zIndex:20}}>
  <div style={{fontSize:15,fontWeight:900,letterSpacing:3,color:WHITE}}>PREPLADDER</div>
  <div style={{fontSize:11,fontWeight:800,letterSpacing:3,color:MUTED}}>NEET PG • COUPON OFFER</div>
</div>;

const Intro: React.FC = () => {
  const f=useCurrentFrame(); const {fps}=useVideoConfig();
  const p=spring({frame:f-4,fps,config:{damping:15,stiffness:110}});
  return <AbsoluteFill style={{justifyContent:"center",padding:"0 90px"}}>
    <Eyebrow text="YOUR NEXT STUDY MOVE" frame={f}/>
    <div style={{marginTop:28,fontSize:100,fontWeight:950,lineHeight:.9,letterSpacing:-5,color:WHITE,
      opacity:clamp(p),transform:`translateY(${interpolate(p,[0,1],[50,0])}px)`}}>
      PREP WITH<br/><span style={{color:"transparent",WebkitTextStroke:`2px ${CYAN}`}}>PURPOSE.</span>
    </div>
    <div style={{marginTop:28,fontSize:22,color:MUTED,letterSpacing:1}}>A smarter checkout starts with one code.</div>
    <div style={{position:"absolute",right:130,top:180,width:250,height:250,borderRadius:38,
      border:"1px solid #65F5FF66",background:"linear-gradient(145deg,#FFFFFF15,#8B5CF610)",
      transform:`perspective(900px) rotateX(${18+Math.sin(f/18)*4}deg) rotateY(${-22+Math.sin(f/22)*6}deg) rotateZ(-9deg)`,
      boxShadow:"0 35px 100px #4F46E544,inset 0 1px 1px #FFFFFF44"}}>
      <div style={{position:"absolute",inset:22,border:"1px solid #FFFFFF20",borderRadius:24}}/>
      <div style={{position:"absolute",left:28,bottom:35,color:CYAN,fontSize:13,fontWeight:800,letterSpacing:3}}>STUDY MODE</div>
      <div style={{position:"absolute",right:28,top:30,fontSize:58,color:WHITE,fontWeight:900}}>↗</div>
    </div>
  </AbsoluteFill>;
};

const Coupon: React.FC = () => {
  const f=useCurrentFrame(); const {fps}=useVideoConfig();
  const p=spring({frame:f-5,fps,config:{damping:12,stiffness:140}});
  const tilt=Math.sin(f/24)*4;
  return <AbsoluteFill style={{alignItems:"center",justifyContent:"center"}}>
    <Eyebrow text="REMEMBER THIS CODE" frame={f}/>
    <div style={{marginTop:22,perspective:1000}}>
      <div style={{width:720,height:310,position:"relative",borderRadius:28,
        background:"linear-gradient(125deg,#161C37 0%,#27245A 48%,#0D263A 100%)",
        border:"1px solid #A5F3FC88",
        boxShadow:"0 40px 120px #22D3EE22, inset 0 1px 1px #FFFFFF55",
        transform:`rotateX(${interpolate(p,[0,1],[24,tilt])}deg) rotateY(${interpolate(p,[0,1],[-24,0])}deg) translateY(${interpolate(p,[0,1],[80,0])}px) scale(${interpolate(p,[0,1],[.78,1])})`,
        opacity:clamp(p),overflow:"hidden"}}>
        <div style={{position:"absolute",inset:-100,background:"linear-gradient(110deg,transparent 35%,#FFFFFF22 48%,transparent 60%)",
          transform:`translateX(${interpolate((f%70),[0,70],[-80,100])}%)`}}/>
        <div style={{position:"absolute",left:34,top:30,color:MUTED,fontSize:13,fontWeight:800,letterSpacing:4}}>PREPLADDER • COUPON</div>
        <div style={{position:"absolute",left:30,right:30,top:72,bottom:30,display:"flex",alignItems:"center",justifyContent:"center"}}>
          <div style={{fontSize:168,fontWeight:950,letterSpacing:8,color:WHITE,
            textShadow:`0 0 40px ${CYAN}55`}}>P650</div>
        </div>
        <div style={{position:"absolute",bottom:25,left:34,color:CYAN,fontSize:13,fontWeight:900,letterSpacing:4}}>ENTER AT CHECKOUT</div>
        <div style={{position:"absolute",right:30,bottom:26,width:50,height:50,borderRadius:14,
          border:"1px solid #65F5FF66",display:"flex",alignItems:"center",justifyContent:"center",color:CYAN,fontSize:24}}>✦</div>
      </div>
    </div>
    <div style={{marginTop:24,color:WHITE,fontSize:20,fontWeight:700,letterSpacing:2}}>P650 — KEEP IT HANDY</div>
  </AbsoluteFill>;
};

const Steps: React.FC = () => {
  const f=useCurrentFrame(); const {fps}=useVideoConfig();
  const items=[["01","CHOOSE YOUR PLAN"],["02","OPEN CHECKOUT"],["03","ENTER P650"],["04","CHECK YOUR FINAL PRICE"]];
  return <AbsoluteFill style={{padding:"0 90px",justifyContent:"center"}}>
    <Eyebrow text="QUICK. SIMPLE. CHECKED." frame={f}/>
    <div style={{fontSize:64,fontWeight:950,letterSpacing:-2,color:WHITE,marginTop:18,marginBottom:32}}>FOUR STEPS TO CHECK.</div>
    <div style={{display:"flex",gap:14}}>
      {items.map(([n,label],i)=>{
        const p=spring({frame:f-4-i*5,fps,config:{damping:16,stiffness:140}});
        return <div key={n} style={{flex:1,height:190,borderRadius:20,padding:20,
          background:"linear-gradient(145deg,#FFFFFF12,#FFFFFF04)",border:`1px solid ${i===2?CYAN:"#FFFFFF25"}`,
          boxShadow:i===2?`0 15px 45px ${CYAN}15`:"none",
          opacity:clamp(p),transform:`translateY(${interpolate(p,[0,1],[35,0])}px) rotateY(${interpolate(p,[0,1],[12,0])}deg)`}}>
          <div style={{fontSize:42,fontWeight:900,color:i===2?CYAN:VIOLET}}>{n}</div>
          <div style={{marginTop:30,fontSize:15,fontWeight:900,lineHeight:1.4,color:WHITE,letterSpacing:1}}>{label}</div>
        </div>;
      })}
    </div>
    <div style={{marginTop:26,color:MUTED,fontSize:17}}>Offers can vary by plan. Verify the price before payment.</div>
  </AbsoluteFill>;
};

const End: React.FC = () => {
  const f=useCurrentFrame(); const {fps}=useVideoConfig();
  const p=spring({frame:f-3,fps,config:{damping:13,stiffness:125}});
  return <AbsoluteFill style={{alignItems:"center",justifyContent:"center",textAlign:"center"}}>
    <div style={{width:440,height:440,borderRadius:"50%",border:"1px solid #65F5FF44",
      boxShadow:"0 0 100px #22D3EE18,inset 0 0 80px #8B5CF618",position:"absolute",
      transform:`scale(${1+Math.sin(f/20)*.035}) rotate(${f*.2}deg)`}}/>
    <Eyebrow text="PREPLADDER COUPON CODE" frame={f}/>
    <div style={{marginTop:20,fontSize:190,fontWeight:950,letterSpacing:4,color:CYAN,
      textShadow:"0 0 60px #22D3EE44",transform:`translateY(${interpolate(p,[0,1],[35,0])}px) scale(${interpolate(p,[0,1],[.8,1])})`}}>P650</div>
    <div style={{marginTop:12,fontSize:22,fontWeight:900,letterSpacing:4,color:WHITE}}>APPLY AT CHECKOUT</div>
    <div style={{marginTop:26,fontSize:15,letterSpacing:3,color:MUTED}}>CHECK THE OFFER • CHOOSE YOUR PLAN</div>
  </AbsoluteFill>;
};

const Scene: React.FC<{start:number;end:number;component:React.FC}> = ({start,end,component:Component}) => {
  const f=useCurrentFrame();
  const {fps}=useVideoConfig();
  const local=f-start;
  const opacity=interpolate(f,[start,start+12,end-12,end],[0,1,1,0],{extrapolateLeft:"clamp",extrapolateRight:"clamp",easing:Easing.inOut(Easing.quad)});
  return <div style={{position:"absolute",inset:0,opacity}}><Component key={start}/></div>;
};

export const P650Promo: React.FC = () => {
  const f=useCurrentFrame();
  const scenes=[{start:0,end:120,component:Intro},{start:120,end:300,component:Coupon},{start:300,end:480,component:Steps},{start:480,end:720,component:End}];
  return <AbsoluteFill style={{background:BG,color:WHITE,fontFamily:"Arial, Helvetica, sans-serif",overflow:"hidden"}}>
    <Backdrop/><Brand/>
    {scenes.map(s=><Scene key={s.start} {...s}/>)}
    <div style={{position:"absolute",left:54,right:54,bottom:24,height:2,background:"#FFFFFF22",zIndex:30}}>
      <div style={{height:"100%",width:`${(f/720)*100}%`,background:`linear-gradient(90deg,${VIOLET},${CYAN})`}}/>
    </div>
    <div style={{position:"absolute",left:54,bottom:34,fontSize:9,fontWeight:800,letterSpacing:3,color:"#FFFFFF66",zIndex:30}}>PREPLADDER • P650</div>
    <div style={{position:"absolute",right:54,bottom:34,fontSize:9,fontWeight:800,letterSpacing:3,color:"#FFFFFF66",zIndex:30}}>OFFER DETAILS AT CHECKOUT</div>
  </AbsoluteFill>;
};
