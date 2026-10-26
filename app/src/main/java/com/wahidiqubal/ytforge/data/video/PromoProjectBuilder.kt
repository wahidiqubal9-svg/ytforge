package com.wahidiqubal.ytforge.data.video

import com.wahidiqubal.ytforge.data.chat.TextSanitizer
import com.wahidiqubal.ytforge.data.voice.VoiceResult
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
)

object PromoProjectBuilder {
    fun build(spec: PromoSpec, voice: VoiceResult): ByteArray {
        val (width, height) = when (spec.ratio) {
            "16:9" -> 1280 to 720
            "1:1" -> 720 to 720
            else -> 720 to 1280
        }
        val duration = ((voice.durationMs / 1000.0) + 0.9).coerceAtLeast(2.0)
        val image = spec.imagePaths.firstOrNull()?.let(::File)?.takeIf { it.isFile }

        val html = buildHtml(
            spec = spec,
            width = width,
            height = height,
            duration = duration,
            hasImage = image != null,
        )

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
        }
        return out.toByteArray()
    }

    private fun buildHtml(
        spec: PromoSpec,
        width: Int,
        height: Int,
        duration: Double,
        hasImage: Boolean,
    ): String {
        val headline = TextSanitizer.short(spec.headline.ifBlank { "Special Offer" }).escape()
        val headlineWords = headline.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { "<span class=\"word\">$it</span>" }
        val subheadline = TextSanitizer.short(spec.subheadline.ifBlank { "A limited-time deal worth checking out." })
            .escape()
            .replace("\n", "<br/>")
        val code = TextSanitizer.coupon(spec.code)
        val cta = TextSanitizer.short(spec.cta.ifBlank { "Tap the link for details." }).escape()
        val codeBlock = if (code.isBlank()) "" else """
            <div id="code" class="code">
              <span class="code-label">USE CODE</span>
              <strong>$code</strong>
            </div>
        """.trimIndent()
        val ctaBlock = """
            <div id="cta" class="cta">$cta</div>
        """.trimIndent()
        val background = if (hasImage) {
            """<img id="bg" class="bg" src="image.jpg" />"""
        } else {
            """<div id="bg" class="bg fallback"></div>"""
        }
        val seconds = String.format(Locale.US, "%.2f", duration)
        val ctaAt = (duration - 1.8).coerceAtLeast(0.6)
        val ctaAtText = String.format(Locale.US, "%.2f", ctaAt)
        val font = if (width > height) "clamp(36px, 8.5vh, 100px)" else "clamp(42px, 8vh, 104px)"

        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=$width, height=$height" />
              <script src="https://cdn.jsdelivr.net/npm/gsap@3.14.2/dist/gsap.min.js"></script>
              <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                html, body { width:${width}px; height:${height}px; overflow:hidden; background:#07111f; font-family:system-ui,-apple-system,sans-serif; }
                #root { position:relative; width:${width}px; height:${height}px; overflow:hidden; background:#07111f; }
                .bg { position:absolute; inset:0; width:100%; height:100%; object-fit:cover; transform-origin:center; }
                .fallback { background:radial-gradient(circle at 70% 10%, #582E9C 0%, #1b1233 48%, #07111f 100%); }
                .scrim { position:absolute; inset:0; background:linear-gradient(180deg, rgba(3,8,18,.10) 0%, rgba(3,8,18,.30) 45%, rgba(3,8,18,.92) 100%); }
                .content { position:absolute; left:6%; right:6%; bottom:6.5%; color:#fff; }
                .word { display:inline-block; }
                .kicker { color:#3EE6B0; font-size:clamp(14px, 1.7vh, 22px); font-weight:800; letter-spacing:.20em; text-transform:uppercase; margin-bottom:2.4vh; }
                h1 { font-size:$font; line-height:.98; letter-spacing:-.045em; max-width:92%; font-weight:900; text-shadow:0 4px 28px rgba(0,0,0,.42); }
                .sub { color:#e5edf7; font-size:clamp(16px, 2.1vh, 30px); line-height:1.35; margin-top:2.2vh; max-width:88%; text-shadow:0 2px 18px rgba(0,0,0,.45); }
                .code { display:flex; align-items:center; gap:2.2vw; width:max-content; max-width:100%; background:#582E9C; border-radius:16px; padding:1.5vh 2.4vw; margin-top:3vh; box-shadow:0 10px 34px rgba(0,0,0,.28); }
                .code-label { color:#fff; font-size:clamp(12px, 1.4vh, 18px); font-weight:800; letter-spacing:.16em; opacity:.88; }
                .code strong { color:#FFD700; font-size:clamp(30px, 4.5vh, 58px); line-height:1; letter-spacing:.02em; font-weight:900; }
                .cta { margin-top:2.8vh; color:#fff; font-size:clamp(14px, 1.8vh, 23px); font-weight:600; opacity:.9; }
                .cta:before { content:"↗ "; color:#3EE6B0; }
              </style>
            </head>
            <body>
              <div id="root" data-composition-id="main" data-start="0" data-duration="$seconds" data-width="$width" data-height="$height" data-fps="30">
                $background
                <div class="scrim"></div>
                <div class="content">
                  <div id="kicker" class="kicker">SPECIAL OFFER</div>
                  <h1 id="headline">$headlineWords</h1>
                  <div id="sub" class="sub">$subheadline</div>
                  $codeBlock
                  $ctaBlock
                </div>
                <audio src="voice.wav" data-start="0" data-duration="$seconds"></audio>
              </div>
              <script>
                const tl = gsap.timeline({ paused: true });
                tl.fromTo("#bg", { scale: 1.06 }, { scale: 1, duration: $seconds, ease: "none" }, 0);
                tl.fromTo("#kicker", { opacity: 0, y: 18 }, { opacity: 1, y: 0, duration: .5, ease: "power3.out" }, .1);
                tl.fromTo(".word", { opacity: 0, y: 34 }, { opacity: 1, y: 0, duration: .55, stagger: .055, ease: "power4.out" }, .24);
                tl.fromTo("#sub", { opacity: 0, y: 20 }, { opacity: 1, y: 0, duration: .55, ease: "power3.out" }, .52);
                ${if (code.isBlank()) "" else """tl.fromTo("#code", { opacity: 0, y: 24, scale: .9 }, { opacity: 1, y: 0, scale: 1, duration: .5, ease: "back.out(1.5)" }, .78);"""}
                tl.fromTo("#cta", { opacity: 0, y: 12 }, { opacity: 1, y: 0, duration: .55, ease: "power3.out" }, $ctaAtText);
                window.__timelines = window.__timelines || {};
                window.__timelines.main = tl;
                tl.seek(0);
              </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun String.escape(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
