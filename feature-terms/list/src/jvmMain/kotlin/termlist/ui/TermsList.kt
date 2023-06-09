package termlist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.TermModel

@Composable
fun TermsList(
    modifier: Modifier = Modifier,
    terms: List<TermModel> = emptyList(),
    current: TermModel? = null,
    onSelected: ((TermModel) -> Unit)? = null,
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(
                    color = Color.White,
                )
                .padding(Dp.Hairline),
        ) {
            items(terms) { term ->
                TermCell(
                    lemma = term.lemma.takeIf { it.isNotEmpty() },
                    selected = term.id == current?.id && term.entryId == current.entryId,
                    onSelected = {
                        onSelected?.invoke(term)
                    },
                )
            }
        }
    }
}
