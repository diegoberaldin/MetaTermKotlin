package dialogstatistics.ui

import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface TermbaseStatisticsComponent {
    val uiState: StateFlow<TermbaseStatisticsUiState>

    fun load(termbase: TermbaseModel)
}