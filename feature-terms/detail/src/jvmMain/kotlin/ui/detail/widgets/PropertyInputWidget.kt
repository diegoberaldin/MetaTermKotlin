package ui.detail.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.PropertyType
import localized
import ui.components.CustomOpenFileDialog
import ui.components.CustomSpinner
import ui.theme.Spacing
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PropertyInputWidget(
    name: String = "",
    value: String = "",
    picklistValues: List<String> = emptyList(),
    type: PropertyType? = null,
    color: Color,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = color, shape = RoundedCornerShape(4.dp))
            .background(color = Color.White.copy(alpha = 0.75f), shape = RoundedCornerShape(4.dp))
            .border(color = color, width = Dp.Hairline, shape = RoundedCornerShape(4.dp))
            .padding(all = Spacing.s),
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = Spacing.xxs),
        ) {
            Icon(
                modifier = Modifier.size(16.dp).onClick {
                    onDelete?.invoke()
                },
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = color,
            )
        }

        Column(
            modifier = Modifier.padding(top = Spacing.s),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxWidth(),
            ) {
                if (name.isNotEmpty()) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.caption,
                        color = color,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                }
                if (type == PropertyType.TEXT) {
                    BasicTextField(
                        modifier = Modifier.fillMaxWidth()
                            .height(40.dp)
                            .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
                        value = value,
                        onValueChange = { it: String ->
                            onValueChange?.invoke(it)
                        },
                        textStyle = MaterialTheme.typography.caption,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Dp.Hairline),
                                contentAlignment = Alignment.TopStart,
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = "insert_value_placeholder".localized(),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                } else if (type == PropertyType.PICKLIST) {
                    CustomSpinner(
                        modifier = Modifier.fillMaxWidth().height(28.dp).fillMaxWidth(),
                        values = picklistValues,
                        current = value,
                        onValueChanged = {
                            val newValue = picklistValues[it]
                            onValueChange?.invoke(newValue)
                        },
                    )
                } else if (type == PropertyType.IMAGE) {
                    var fileDialogOpen by remember {
                        mutableStateOf(false)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
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
                        Button(
                            modifier = Modifier.heightIn(max = 25.dp),
                            contentPadding = PaddingValues(vertical = 0.dp, horizontal = Spacing.s),
                            onClick = {
                                if (!fileDialogOpen) {
                                    fileDialogOpen = true
                                }
                            },
                        ) {
                            Text(
                                text = "term_detail_button_pick_image".localized(),
                                style = MaterialTheme.typography.caption,
                            )
                        }
                    }
                    if (fileDialogOpen) {
                        CustomOpenFileDialog(
                            title = "dialog_title_select_image".localized(),
                            nameFilter = { it.endsWith("png") || it.endsWith("jpg") || it.endsWith("jpeg") },
                            onCloseRequest = { path ->
                                if (path != null) {
                                    onValueChange?.invoke(path)
                                }
                                fileDialogOpen = false
                            },
                        )
                    }
                }
            }
        }
    }
}
