package com.wahidiqubal.ytforge.data.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.wahidiqubal.ytforge.data.chat.TextSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object ThumbnailGenerator {
    private const val W = 1280
    private const val H = 720

    fun styleName(variant: Int): String = when ((variant % 4 + 4) % 4) {
        1 -> "Split layout"
        2 -> "Code hero"
        3 -> "Editorial"
        else -> "Cinematic"
    }

    suspend fun generate(
        context: Context,
        imagePath: String?,
        headline: String,
        subheadline: String,
        code: String,
        cta: String,
        variant: Int = 0,
    ): File? = withContext(Dispatchers.IO) {
        val style = (variant % 4 + 4) % 4
        val cleanHeadline = TextSanitizer.short(headline.ifBlank { "Special Offer" })
        val cleanSub = TextSanitizer.short(subheadline)
        val cleanCode = TextSanitizer.coupon(code)
        val cleanCta = TextSanitizer.short(cta.ifBlank { "Tap the link for details." })
        val accent = when (style) {
            1 -> 0xFF7FD7FF.toInt()
            2 -> 0xFFFFD700.toInt()
            3 -> 0xFFFF8A65.toInt()
            else -> 0xFF3EE6B0.toInt()
        }
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawBase(canvas, imagePath, style)

        if (style == 1) {
            val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xF20B1726.toInt() }
            canvas.drawRect(0f, 0f, 610f, H.toFloat(), panel)
            val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
            canvas.drawRect(40f, 130f, 48f, 650f, line)
        }

        val layout = when (style) {
            1 -> LayoutSpec(
                headlineX = 82f, headlineY = 170f, headlineWidth = 470, headlineSize = 60f,
                subX = 82f, subY = 410f, subWidth = 470,
                codeX = 82f, codeY = 510f, codeWidth = 400, codeSize = 48f,
                ctaX = 82f, ctaY = 680f,
            )
            2 -> LayoutSpec(
                headlineX = 72f, headlineY = 105f, headlineWidth = 980, headlineSize = 76f,
                subX = 72f, subY = 360f, subWidth = 900,
                codeX = 390f, codeY = 455f, codeWidth = 500, codeSize = 82f,
                ctaX = 72f, ctaY = 685f,
            )
            3 -> LayoutSpec(
                headlineX = 72f, headlineY = 140f, headlineWidth = 900, headlineSize = 70f,
                subX = 72f, subY = 385f, subWidth = 800,
                codeX = 778f, codeY = 535f, codeWidth = 430, codeSize = 50f,
                ctaX = 72f, ctaY = 685f,
            )
            else -> LayoutSpec(
                headlineX = 72f, headlineY = 190f, headlineWidth = 1040, headlineSize = 84f,
                subX = 72f, subY = 465f, subWidth = 1040,
                codeX = 72f, codeY = 535f, codeWidth = 430, codeSize = 54f,
                ctaX = 72f, ctaY = 690f,
            )
        }

        val kickerPaint = textPaint(accent, 24f, bold = true, spacing = 0.18f)
        canvas.drawText("SPECIAL OFFER", layout.headlineX, 100f, kickerPaint)

        val headlinePaint = textPaint(Color.WHITE, layout.headlineSize, bold = true)
        val headlineLayout = StaticLayout.Builder
            .obtain(cleanHeadline, 0, cleanHeadline.length, headlinePaint, layout.headlineWidth)
            .setMaxLines(3)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        canvas.save()
        canvas.translate(layout.headlineX, layout.headlineY)
        headlineLayout.draw(canvas)
        canvas.restore()

        if (cleanSub.isNotBlank() && style != 2) {
            val subPaint = textPaint(0xFFE5EDF7.toInt(), 32f, bold = false)
            val subLayout = StaticLayout.Builder
                .obtain(cleanSub, 0, cleanSub.length, subPaint, layout.subWidth)
                .setMaxLines(2)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()
            canvas.save()
            canvas.translate(layout.subX, layout.subY)
            subLayout.draw(canvas)
            canvas.restore()
        }

        if (cleanCode.isNotBlank()) {
            drawCode(canvas, layout, cleanCode, style, accent)
        }

        val ctaPaint = textPaint(Color.WHITE, 28f, bold = true)
        canvas.drawText("↗", layout.ctaX, layout.ctaY, ctaPaint.apply { color = accent })
        canvas.drawText(cleanCta.take(60), layout.ctaX + 42f, layout.ctaY, ctaPaint.apply { color = Color.WHITE })

        saveJpeg(context, bitmap)
    }

    suspend fun normalize(context: Context, sourcePath: String): File? = withContext(Dispatchers.IO) {
        val source = BitmapFactory.decodeFile(sourcePath) ?: return@withContext null
        val target = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(target)
        drawCover(canvas, source, W, H)
        source.recycle()
        saveJpeg(context, target)
    }

    private fun drawBase(canvas: Canvas, imagePath: String?, style: Int) {
        val background = imagePath?.let { BitmapFactory.decodeFile(it) }
        if (background != null) {
            drawCover(canvas, background, W, H)
            background.recycle()
        } else {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val colors = when (style) {
                1 -> intArrayOf(0xFF173A5E.toInt(), 0xFF0B1E33.toInt(), 0xFF07111F.toInt())
                2 -> intArrayOf(0xFF5A2A0A.toInt(), 0xFF2A160B.toInt(), 0xFF07111F.toInt())
                3 -> intArrayOf(0xFF4A1D2A.toInt(), 0xFF241017.toInt(), 0xFF07111F.toInt())
                else -> intArrayOf(0xFF582E9C.toInt(), 0xFF1B1233.toInt(), 0xFF07111F.toInt())
            }
            paint.shader = LinearGradient(
                0f, 0f, W.toFloat(), H.toFloat(), colors,
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
            )
            canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), paint)
        }
        val scrim = Paint(Paint.ANTI_ALIAS_FLAG)
        scrim.shader = LinearGradient(
            0f, 0f, 0f, H.toFloat(),
            intArrayOf(0x08000000, 0x33000000, if (style == 2) 0xF2000000.toInt() else 0xE6000000.toInt()),
            floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), scrim)
    }

    private fun drawCode(canvas: Canvas, layout: LayoutSpec, code: String, style: Int, accent: Int) {
        val boxX = layout.codeX
        val boxWidth = layout.codeWidth.toFloat()
        val box = RectF(boxX, layout.codeY, boxX + boxWidth, layout.codeY + 110f)
        val background = when (style) {
            1 -> 0xFF173A5E.toInt()
            2 -> 0xFF5A2A0A.toInt()
            3 -> 0xFF4A1D2A.toInt()
            else -> 0xFF582E9C.toInt()
        }
        canvas.drawRoundRect(box, 18f, 18f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = background })
        val label = textPaint(Color.WHITE, 20f, bold = true, spacing = 0.12f)
        canvas.drawText("USE CODE", boxX + 24f, layout.codeY + 38f, label)
        val codePaint = textPaint(if (style == 2) Color.WHITE else 0xFFFFD700.toInt(), layout.codeSize, bold = true)
        canvas.drawText(code, boxX + 24f, layout.codeY + 95f, codePaint)
        if (style == 3) {
            canvas.drawRect(boxX - 12f, layout.codeY, boxX - 4f, layout.codeY + 110f, Paint().apply { color = accent })
        }
    }

    private fun saveJpeg(context: Context, bitmap: Bitmap): File {
        val dir = File(context.filesDir, "thumbnails").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out) }
        bitmap.recycle()
        return file
    }

    private fun drawCover(canvas: Canvas, bitmap: Bitmap, width: Int, height: Int) {
        val scale = maxOf(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
        val scaledWidth = bitmap.width * scale
        val scaledHeight = bitmap.height * scale
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(left, top, left + scaledWidth, top + scaledHeight),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true },
        )
    }

    private fun textPaint(color: Int, size: Float, bold: Boolean, spacing: Float = 0f): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            letterSpacing = spacing
        }

    private data class LayoutSpec(
        val headlineX: Float,
        val headlineY: Float,
        val headlineWidth: Int,
        val headlineSize: Float,
        val subX: Float,
        val subY: Float,
        val subWidth: Int,
        val codeX: Float,
        val codeY: Float,
        val codeWidth: Int,
        val codeSize: Float,
        val ctaX: Float,
        val ctaY: Float,
    )
}
