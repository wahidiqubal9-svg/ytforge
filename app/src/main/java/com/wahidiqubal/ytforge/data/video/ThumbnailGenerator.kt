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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object ThumbnailGenerator {
    suspend fun generate(
        context: Context,
        imagePath: String?,
        headline: String,
        subheadline: String,
        code: String,
        cta: String,
        variant: Int = 0,
    ): File? = withContext(Dispatchers.IO) {
        val width = 1280
        val height = 720
        val variantIndex = ((variant % 3) + 3) % 3
        val accent = when (variantIndex) {
            1 -> 0xFF7FD7FF.toInt()
            2 -> 0xFFFFC857.toInt()
            else -> 0xFF3EE6B0.toInt()
        }
        val codeBackground = when (variantIndex) {
            1 -> 0xFF173A5E.toInt()
            2 -> 0xFF5A2A0A.toInt()
            else -> 0xFF582E9C.toInt()
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val background = imagePath?.let { BitmapFactory.decodeFile(it) }
        if (background != null) {
            drawCover(canvas, background, width, height)
            background.recycle()
        } else {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val colors = when (variantIndex) {
                1 -> intArrayOf(0xFF173A5E.toInt(), 0xFF0B1E33.toInt(), 0xFF07111F.toInt())
                2 -> intArrayOf(0xFF5A2A0A.toInt(), 0xFF2A160B.toInt(), 0xFF07111F.toInt())
                else -> intArrayOf(0xFF582E9C.toInt(), 0xFF1B1233.toInt(), 0xFF07111F.toInt())
            }
            paint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                colors,
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

        val scrim = Paint(Paint.ANTI_ALIAS_FLAG)
        scrim.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(0x08000000, 0x33000000.toInt(), 0xE6000000.toInt()),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrim)

        val kickerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 24f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = 0.18f
        }
        canvas.drawText("SPECIAL OFFER", 72f, 150f, kickerPaint)

        val headlinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = if (headline.length > 48) 70f else 84f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        val headlineLayout = StaticLayout.Builder
            .obtain(headline.ifBlank { "Special Offer" }, 0, headline.ifBlank { "Special Offer" }.length, headlinePaint, 1040)
            .setMaxLines(3)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        canvas.save()
        canvas.translate(72f, 185f)
        headlineLayout.draw(canvas)
        canvas.restore()

        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE5EDF7.toInt()
            textSize = 34f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val subLayout = StaticLayout.Builder
            .obtain(subheadline.ifBlank { "A limited-time offer." }, 0, subheadline.ifBlank { "A limited-time offer." }.length, subPaint, 1040)
            .setMaxLines(2)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        canvas.save()
        canvas.translate(72f, 465f)
        subLayout.draw(canvas)
        canvas.restore()

        if (code.isNotBlank()) {
            val boxX = if (variantIndex == 1) 778f else 72f
            val box = RectF(boxX, 535f, boxX + 430f, 645f)
            val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = codeBackground }
            canvas.drawRoundRect(box, 18f, 18f, boxPaint)
            val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                letterSpacing = 0.12f
            }
            canvas.drawText("USE CODE", boxX + 24f, 575f, labelPaint)
            val codePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (variantIndex == 2) 0xFFFFFFFF.toInt() else 0xFFFFD700.toInt()
                textSize = 54f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            canvas.drawText(code, boxX + 24f, 628f, codePaint)
            if (variantIndex == 1) {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
                canvas.drawRect(40f, 170f, 48f, 650f, linePaint)
            }
        }

        val ctaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        canvas.drawText("↗ ", 72f, 690f, ctaPaint.apply { color = accent })
        canvas.drawText(cta.ifBlank { "Tap the link for details." }, 112f, 690f, ctaPaint.apply { color = Color.WHITE })

        val dir = File(context.filesDir, "thumbnails").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out) }
        bitmap.recycle()
        file
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
}
