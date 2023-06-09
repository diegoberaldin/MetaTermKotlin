package common.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import common.utils.toLocalPixel
import common.utils.toRadians
import java.text.DecimalFormat
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun CustomProgressIndicator(
    progress: Float,
    progressColor: Color = MaterialTheme.colors.primary,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        val textMeasurer = rememberTextMeasurer()
        val decimalFormat = DecimalFormat("#.#")
        val progressText = "${decimalFormat.format(progress * 100)}%"

        Box(
            modifier = Modifier.fillMaxWidth(progress)
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .background(
                    color = progressColor,
                    shape = RoundedCornerShape(4.dp),
                ),
        )

        val stripeWidth = 20.dp.toLocalPixel()
        val clipRectCornerRadius = 4.dp.toLocalPixel()
        val stripeRotateAngle = 45f
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val backgroundString = buildAnnotatedString {
                withStyle(SpanStyle(color = progressColor)) {
                    append(progressText)
                }
            }
            val textOffsetTop = (size.height - textMeasurer.measure(backgroundString).size.height) * 0.5f
            val textOffsetLeft = (size.width - textMeasurer.measure(backgroundString).size.width) * 0.5f
            drawText(
                textMeasurer = textMeasurer,
                text = backgroundString,
                topLeft = Offset(x = textOffsetLeft, y = textOffsetTop),
            )
            val pathToClip = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset.Zero,
                            size = Size(width = size.width * progress, height = size.height),
                        ),
                        cornerRadius = CornerRadius(x = clipRectCornerRadius, y = clipRectCornerRadius),
                    ),
                )
            }
            clipPath(path = pathToClip) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) {
                            append(progressText)
                        }
                    },
                    topLeft = Offset(x = textOffsetLeft, y = textOffsetTop),
                )

                var stripeOffset = -stripeWidth
                val stripeHeightDelta = stripeWidth * sin(stripeRotateAngle.toRadians())
                while (stripeOffset < size.width * progress) {
                    val rect = Rect(
                        offset = Offset(x = stripeOffset, y = -stripeHeightDelta),
                        size = Size(
                            width = stripeWidth,
                            height = size.height + 2 * stripeHeightDelta,
                        ),
                    )
                    rotate(degrees = stripeRotateAngle, pivot = rect.center) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.25f),
                            topLeft = rect.topLeft,
                            size = rect.size,
                        )
                    }
                    stripeOffset += stripeWidth
                }
            }
        }
    }
}
