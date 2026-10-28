package com.wahidiqubal.ytforge.data.video

import com.wahidiqubal.ytforge.data.chat.TextSanitizer
import com.wahidiqubal.ytforge.data.voice.VoiceResult
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class PromoSpec(
    val headline: String,
    val subheadline: String,
    val code: String,
    val cta: String,
    val narration: String,
    val ratio: String,
    val imagePaths: List<String>,
    val title: String,
    val description: String,
    val tags: String,
    val videoStyle: String = "kinetic",
    val engine: String = "remotion",
)

object PromoProjectBuilder {
    fun build(spec: PromoSpec, voice: VoiceResult): ByteArray {
        val (width, height) = when (spec.ratio) {
            "16:9" -> 1280 to 720
            "1:1" -> 720 to 720
            else -> 720 to 1280
        }
        val duration = ((voice.durationMs / 1000.0) + 0.9).coerceAtLeast(3.0)
        val image = spec.imagePaths.firstOrNull()?.let(::File)?.takeIf { it.isFile }
        val html = buildHtml(spec, width, height, duration, image != null)

        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("index.html"))
            zip.write(html.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("voice.wav"))
            zip.write(voice.wav)
            zip.closeEntry()
            if (image != null) {
                zip.putNextEntry(ZipEntry("image.jpg"))
                zip.write(image.readBytes())
                zip.closeEntry()
            }

            val frames = (((voice.durationMs / 1000.0) + 0.9) * 30.0).toInt().coerceAtLeast(90)
            val props = buildJsonObject {
                put("headline", TextSanitizer.short(spec.headline.ifBlank { "Special Offer" }))
                put("subheadline", TextSanitizer.short(spec.subheadline))
                put("code", TextSanitizer.coupon(spec.code))
                put("cta", TextSanitizer.short(spec.cta))
                put("title", TextSanitizer.short(spec.title.ifBlank { spec.headline }))
                put("styleId", spec.videoStyle)
                put("ratio", spec.ratio)
                put("durationInFrames", frames)
                put("audio", "voice.wav")
                if (image != null) put("image", "image.jpg")
            }
            zip.putNextEntry(ZipEntry("props.json"))
            zip.write(props.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return out.toByteArray()
    }

    private fun buildHtml(spec: PromoSpec, width: Int, height: Int, duration: Double, hasImage: Boolean): String {
        val style = VideoStyles.byId(spec.videoStyle)
        val headline = TextSanitizer.short(spec.headline.ifBlank { "Special Offer" })
        val headlineHtml = headline.split(Regex("\\s+")).filter { it.isNotBlank() }
            .joinToString(" ") { "<span class=\"word\">${it.escape()}</span>" }
        val sub = TextSanitizer.short(spec.subheadline.ifBlank { "Watch the offer." }).escape()
        val code = TextSanitizer.coupon(spec.code)
        val cta = TextSanitizer.short(spec.cta.ifBlank { "Tap for details." }).escape()
        val title = TextSanitizer.short(spec.title.ifBlank { headline }).escape()
        val s1 = (duration * 0.30).coerceAtLeast(1.4)
        val s2 = (duration * 0.40).coerceAtLeast(1.4)
        val s3 = (duration - s1 - s2).coerceAtLeast(1.2)
        val t1 = 0.0
        val t2 = s1
        val t3 = s1 + s2
        val seconds = { v: Double -> String.format(Locale.US, "%.2f", v) }
        val portrait = height > width
        val cardsDirection = if (portrait) "column" else "row"
        val cardWidth = if (portrait) "100%" else "31%"
        val bg = if (hasImage) """<img id="bg" src="image.jpg" />""" else """<div id="bg" class="fallback"></div>"""
        val scrim = if (style.light) {
            "linear-gradient(180deg, rgba(255,255,255,.12) 0%, rgba(255,255,255,.55) 46%, rgba(255,255,255,.94) 100%)"
        } else {
            "linear-gradient(180deg, rgba(3,8,18,.08) 0%, rgba(3,8,18,.34) 45%, rgba(3,8,18,.94) 100%)"
        }
        val kickerColor = if (style.light) style.accent else style.accent
        val ctaText = "↗ $cta"
        val codeLabel = if (code.isBlank()) "OFFER" else "CODE"
        val codeValue = if (code.isBlank()) "TODAY" else code
        val saveValue = sub.take(34)
        val nextValue = cta.take(24)

        return """
<!doctype html>
<html><head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=$width, height=$height" />
<script src="https://cdn.jsdelivr.net/npm/gsap@3.14.2/dist/gsap.min.js"></script>
<style>
*{margin:0;padding:0;box-sizing:border-box}
html,body{width:${width}px;height:${height}px;overflow:hidden;background:${style.bgA};font-family:system-ui,-apple-system,sans-serif}
#root{position:relative;width:${width}px;height:${height}px;overflow:hidden;color:${style.ink};
  --bgA:${style.bgA};--bgB:${style.bgB};--accent:${style.accent};--ink:${style.ink};--muted:${style.muted};--card:${style.card};--codeBg:${style.codeBg};--codeInk:${style.codeInk};
  background:radial-gradient(1100px 820px at 78% 8%, var(--bgB) 0%, var(--bgA) 58%, #030812 100%)}
#bg{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;transform-origin:center}
.fallback{position:absolute;inset:0;background:radial-gradient(circle at 72% 12%, var(--bgB) 0%, var(--bgA) 48%, #030812 100%)}
.scrim{position:absolute;inset:0;background:${scrim}}
.scene{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;padding:${if (portrait) "46px" else "54px"};visibility:hidden}
.center{width:100%;max-width:${if (portrait) "620px" else "1080px"};display:flex;flex-direction:column;align-items:flex-start;gap:14px}
.kicker{color:${kickerColor};font-size:clamp(14px,1.7vh,22px);letter-spacing:.20em;text-transform:uppercase;font-weight:800}
h1{font-size:${if (portrait) "clamp(44px,7.4vh,92px)" else "clamp(42px,8.2vh,102px)"};line-height:.98;letter-spacing:-.045em;font-weight:900;max-width:100%}
h1 span{color:${kickerColor}}
.sub{color:${style.muted};font-size:clamp(16px,2.1vh,30px);line-height:1.35;max-width:90%}
.cards{display:flex;flex-direction:${cardsDirection};gap:12px;width:100%;margin-top:6px}
.card{width:${cardWidth};background:${style.card};border:1px solid color-mix(in srgb, ${style.ink} 16%, transparent);border-radius:18px;padding:${if (portrait) "16px" else "18px"};min-width:0}
.card b{display:block;color:${kickerColor};font-size:clamp(24px,3.2vh,44px);line-height:1.05;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.card span{display:block;color:${style.muted};font-size:clamp(12px,1.5vh,18px);margin-top:4px;text-transform:uppercase;letter-spacing:.08em}
.code{display:inline-flex;align-items:center;gap:14px;background:${style.codeBg};color:${style.codeInk};border-radius:16px;padding:12px 18px;margin-top:8px;box-shadow:0 14px 36px rgba(0,0,0,.22)}
.code span{font-size:clamp(11px,1.4vh,16px);font-weight:800;letter-spacing:.14em;color:${style.ink};opacity:.9}
.code strong{font-size:clamp(28px,4.4vh,58px);line-height:1;font-weight:900;color:${style.codeInk}}
.cta{color:${style.ink};font-size:clamp(14px,1.9vh,24px);font-weight:700;margin-top:8px}
.cta em{font-style:normal;color:${kickerColor}}
.bar{width:100%;height:8px;border-radius:99px;background:color-mix(in srgb, ${style.ink} 16%, transparent);margin-top:10px;overflow:hidden}
.bar i{display:block;width:0;height:100%;background:${kickerColor}}
.tag{position:absolute;bottom:32px;left:0;right:0;text-align:center;color:${style.muted};font-size:12px;letter-spacing:.16em;text-transform:uppercase;opacity:.75}
.word{display:inline-block}
</style></head>
<body>
<div id="root" data-composition-id="main" data-start="0" data-duration="${seconds(duration)}" data-width="$width" data-height="$height" data-fps="30">
$bg<div class="scrim"></div>
<div id="s1" class="scene" data-start="0" data-duration="${seconds(s1)}" data-track-index="0">
  <div class="center"><div id="k1" class="kicker">SPECIAL OFFER</div>
    <h1 id="h1">$headlineHtml</h1><div id="p1" class="sub">$sub</div></div>
</div>
<div id="s2" class="scene" data-start="${seconds(t2)}" data-duration="${seconds(s2)}" data-track-index="1">
  <div class="center"><div id="k2" class="kicker">THE OFFER</div>
    <h1 id="h2" style="font-size:${if (portrait) "clamp(34px,5.4vh,66px)" else "clamp(34px,6vh,72px)"}">One offer.<br/><span>Three reasons.</span></h1>
    <div id="cards" class="cards">
      <div class="card"><b>$codeValue</b><span>$codeLabel</span></div>
      <div class="card"><b>$saveValue</b><span>WHY IT MATTERS</span></div>
      <div class="card"><b>$nextValue</b><span>NEXT STEP</span></div>
    </div><div class="bar"><i id="bar"></i></div></div>
</div>
<div id="s3" class="scene" data-start="${seconds(t3)}" data-duration="${seconds(s3)}" data-track-index="2">
  <div class="center"><div id="k3" class="kicker">${if (code.isBlank()) "READY?" else "USE CODE"}</div>
    <h1 id="h3">${if (code.isBlank()) "Check it out.<br/><span>Today.</span>" else "Save more.<br/><span>$code</span>"}</h1>
    <div id="c3" class="cta"><em>$ctaText</em></div>
    <div id="tag3" class="tag">$title</div></div>
</div>
<audio src="voice.wav" data-start="0" data-duration="${seconds(duration)}"></audio>
</div>
<script>
const tl = gsap.timeline({paused:true});
tl.fromTo("#bg",{scale:1.06},{scale:1,duration:${seconds(duration)},ease:"none"},0);
tl.fromTo("#k1",{opacity:0,y:18},{opacity:1,y:0,duration:.5,ease:"power3.out"},.1);
tl.fromTo(".word",{opacity:0,y:34},{opacity:1,y:0,duration:.55,stagger:.055,ease:"power4.out"},.24);
tl.fromTo("#p1",{opacity:0,y:18},{opacity:1,y:0,duration:.5,ease:"power3.out"},.7);
tl.fromTo("#k2",{opacity:0,y:18},{opacity:1,y:0,duration:.45,ease:"power3.out"},${seconds(t2 + .08)});
tl.fromTo("#h2",{opacity:0,y:30},{opacity:1,y:0,duration:.55,ease:"power4.out"},${seconds(t2 + .18)});
tl.fromTo("#cards .card",{opacity:0,y:26},{opacity:1,y:0,duration:.5,stagger:.1,ease:"power3.out"},${seconds(t2 + .32)});
tl.fromTo("#bar i",{width:"0%"},{width:"100%",duration:${seconds(s2 * .8)},ease:"power2.inOut"},${seconds(t2 + .4)});
tl.fromTo("#k3",{opacity:0,y:18},{opacity:1,y:0,duration:.45,ease:"power3.out"},${seconds(t3 + .08)});
tl.fromTo("#h3",{opacity:0,y:32},{opacity:1,y:0,duration:.6,ease:"power4.out"},${seconds(t3 + .18)});
tl.fromTo("#c3",{opacity:0,y:14},{opacity:1,y:0,duration:.5,ease:"power3.out"},${seconds(t3 + .48)});
tl.fromTo("#tag3",{opacity:0},{opacity:1,duration:.4},${seconds(t3 + .7)});
window.__timelines = window.__timelines || {}; window.__timelines.main = tl; tl.seek(0);
</script>
</body></html>
""".trimIndent()
    }

    private fun String.escape(): String = this
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
