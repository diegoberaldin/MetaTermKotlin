package ui.detail.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.PropertyType
import localized
import ui.components.StyledLabel
import ui.theme.Spacing
import java.io.File

@Composable
fun PropertyWidget(
    modifier: Modifier = Modifier,
    color: Color,
    name: String,
    value: String,
    type: PropertyType? = null,
) {
    StyledLabel(
        modifier = modifier,
        color = color,
        title = "term_detail_title_property".localized(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = name,
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                ),
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            if (type == PropertyType.TEXT || type == PropertyType.PICKLIST) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = value,
                    style = MaterialTheme.typography.body2.copy(fontSize = 11.sp),
                )
            } else if (type == PropertyType.IMAGE) {
                if (value.isNotEmpty()) {
                    val imageBitmap: ImageBitmap = remember(value) {
                        val file = File(value)
                        loadImageBitmap(file.inputStream())
                    }
                    Image(
                        modifier = Modifier.padding(Spacing.s).sizeIn(minWidth = 150.dp, maxWidth = 400.dp),
                        painter = BitmapPainter(image = imageBitmap),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
        }
    }
}
