package common.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlin.math.PI

@Composable
fun Dp.toLocalPixel(): Float {
    return with(LocalDensity.current) {
        this@toLocalPixel.toPx()
    }
}

fun Float.toRadians(): Float {
    return (this / 180f * PI).toFloat()
}
