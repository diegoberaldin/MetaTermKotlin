package ui.dialog.statistics

data class TermbaseStatisticsUiState(
    val items: List<TermbaseStatisticsItem> = listOf(),
    val loading: Boolean = false,
)
