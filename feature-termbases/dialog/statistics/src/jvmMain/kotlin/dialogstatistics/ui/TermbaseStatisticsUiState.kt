package dialogstatistics.ui

data class TermbaseStatisticsUiState(
    val items: List<TermbaseStatisticsItem> = listOf(),
    val loading: Boolean = false,
)
