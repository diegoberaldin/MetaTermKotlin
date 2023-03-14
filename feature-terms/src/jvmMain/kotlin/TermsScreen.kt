import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TermsScreen(
    viewModel: TermsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Application title",
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            LazyColumn(modifier = Modifier.weight(0.25f)) {
                items(uiState.terms) { lemma ->
                    TermCell(
                        modifier = Modifier.height(40.dp),
                        lemma = lemma,
                        selected = lemma == uiState.selectedTerm,
                        onSelected = {
                            if (uiState.selectedTerm == lemma) {
                                viewModel.setSelected(null)
                            } else {
                                viewModel.setSelected(lemma)
                            }
                        }
                    )
                }
            }
            Box(
                modifier = Modifier.background(Color.White)
                    .width(1.dp)
                    .fillMaxHeight()
            )
            Box(modifier = Modifier.weight(1f).padding(22.dp)) {
                Text(
                    text = uiState.selectedTerm ?: "",
                    style = MaterialTheme.typography.h5,
                    color = Color.White
                )
            }
        }
    }
}