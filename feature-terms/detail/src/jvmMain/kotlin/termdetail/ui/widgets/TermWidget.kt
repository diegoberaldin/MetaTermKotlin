package termdetail.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import localized
import common.ui.components.StyledLabel
import common.ui.theme.DeepPurple300

@Composable
fun TermWidget(
    modifier: Modifier = Modifier,
    lemma: String,
) {
    StyledLabel(
        modifier = modifier,
        color = DeepPurple300,
        title = "term_detail_title_lemma".localized(),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = lemma,
            style = MaterialTheme.typography.body2.copy(fontSize = 11.sp),
        )
    }
}
