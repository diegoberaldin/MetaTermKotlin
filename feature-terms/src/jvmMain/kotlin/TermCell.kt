import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TermCell(
    modifier: Modifier,
    lemma: String,
    selected: Boolean = false,
    onSelected: (() -> Unit)? = null,
) {
    Box(modifier = modifier
        .background(
            color = if (selected) {
                Color.White.copy(alpha = 0.1f)
            } else {
                Color.Transparent
            }
        )
        .onClick {
            onSelected?.invoke()
        }) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp).fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = lemma,
                style = MaterialTheme.typography.subtitle2,
                color = Color.White
            )
        }
        Box(
            modifier = Modifier.background(Color.White)
                .height(1.dp)
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        )
    }
}