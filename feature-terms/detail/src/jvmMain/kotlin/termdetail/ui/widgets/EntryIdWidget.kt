package termdetail.ui.widgets

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import localized

@Composable
fun EntryIdWidget(
    modifier: Modifier = Modifier,
    entryId: Int,
) {
    Text(
        modifier = modifier,
        text = "${"term_detail_title_entry".localized()}: $entryId",
        style = MaterialTheme.typography.caption,
        color = Color.Gray,
    )
}
