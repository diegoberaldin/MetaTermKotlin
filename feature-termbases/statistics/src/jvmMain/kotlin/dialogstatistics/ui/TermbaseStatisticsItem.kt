package dialogstatistics.ui

sealed interface TermbaseStatisticsItem {
    object Divider : TermbaseStatisticsItem
    data class Header(val title: String = "") : TermbaseStatisticsItem
    data class LanguageHeader(val name: String = "") : TermbaseStatisticsItem
    data class TextRow(val title: String = "", val value: String = "") : TermbaseStatisticsItem
    data class BarChartRow(val title: String = "", val value: Float = 0f) : TermbaseStatisticsItem
}
