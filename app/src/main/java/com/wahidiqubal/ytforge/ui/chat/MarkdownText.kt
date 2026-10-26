package com.wahidiqubal.ytforge.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import com.wahidiqubal.ytforge.ui.theme.OutlineDark
import com.wahidiqubal.ytforge.ui.theme.SurfaceDeep
import com.wahidiqubal.ytforge.ui.theme.SurfaceRaised

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(text) { MarkdownLite.parse(text) }
    SelectionContainer {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            blocks.forEach { block ->
                when (block) {
                    is MarkdownBlock.Heading -> HeadingBlock(block)
                    is MarkdownBlock.Paragraph -> InlineText(block.inlines)
                    is MarkdownBlock.BulletList -> BulletListBlock(block)
                    is MarkdownBlock.OrderedList -> OrderedListBlock(block)
                    is MarkdownBlock.CodeBlock -> CodeBlock(block)
                    is MarkdownBlock.Blockquote -> BlockquoteBlock(block)
                    is MarkdownBlock.Table -> TableBlock(block)
                    is MarkdownBlock.HorizontalRule -> HorizontalDivider(
                        color = OutlineDark,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineText(
    inlines: List<MarkdownInline>,
    modifier: Modifier = Modifier,
    color: Color = Ink,
    fontSize: Int = 16,
    lineHeight: Int = 24,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Text(
        text = renderInline(inlines),
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = color,
            fontSize = fontSize.sp,
            lineHeight = lineHeight.sp,
            fontWeight = fontWeight,
        ),
    )
}

@Composable
private fun HeadingBlock(block: MarkdownBlock.Heading) {
    val size = when (block.level) {
        1 -> 27
        2 -> 23
        3 -> 20
        else -> 18
    }
    val line = (size * 1.25).toInt()
    Row {
        InlineText(
            inlines = block.inlines,
            color = Ink,
            fontSize = size,
            lineHeight = line,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = if (block.level == 1) 4.dp else 0.dp),
        )
    }
}

@Composable
private fun BulletListBlock(block: MarkdownBlock.BulletList) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        block.items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "•",
                    color = Accent,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(16.dp),
                )
                InlineText(item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OrderedListBlock(block: MarkdownBlock.OrderedList) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        block.items.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${index + 1}.",
                    color = Accent,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(26.dp),
                )
                InlineText(item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CodeBlock(block: MarkdownBlock.CodeBlock) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDeep,
        border = BorderStroke(1.dp, OutlineDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = block.code,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Ink,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                ),
            )
        }
    }
}

@Composable
private fun BlockquoteBlock(block: MarkdownBlock.Blockquote) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(42.dp)
                .padding(end = 10.dp),
        ) {
            Surface(color = Accent, modifier = Modifier.fillMaxWidth()) {}
        }
        InlineText(block.inlines, color = Muted, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TableBlock(block: MarkdownBlock.Table) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDeep,
        border = BorderStroke(1.dp, OutlineDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    block.headers.forEach { header ->
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelLarge,
                            color = Accent,
                            modifier = Modifier.widthIn(min = 90.dp),
                        )
                    }
                }
                HorizontalDivider(color = OutlineDark)
                block.rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { cell ->
                            Text(
                                text = cell,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink,
                                modifier = Modifier.widthIn(min = 90.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun renderInline(inlines: List<MarkdownInline>): AnnotatedString = buildAnnotatedString {
    fun appendInlines(nodes: List<MarkdownInline>) {
        nodes.forEach { inline ->
            when (inline) {
                is MarkdownInline.Plain -> append(inline.text)
                is MarkdownInline.Bold ->
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { appendInlines(inline.inlines) }
                is MarkdownInline.Italic ->
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { appendInlines(inline.inlines) }
                is MarkdownInline.Strikethrough ->
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { appendInlines(inline.inlines) }
                is MarkdownInline.Code ->
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = SurfaceRaised,
                            color = Accent,
                        ),
                    ) { append(inline.text) }
                is MarkdownInline.Link -> {
                    val url = inline.url
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        withLink(
                            LinkAnnotation.Url(
                                url,
                                TextLinkStyles(
                                    style = SpanStyle(
                                        color = Accent,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                                ),
                            ),
                        ) { append(inline.text) }
                    } else {
                        append(inline.text)
                    }
                }
                is MarkdownInline.Image -> append(inline.text.ifBlank { "[image]" })
            }
        }
    }
    appendInlines(inlines)
}
