package ui.dialog.filter

data class TermFilterUiState(
    val currentName: String = "",
    val availableMatchTypes: List<MatchType> = emptyList(),
    val currentMatchType: MatchType? = null,
    val currentValue: String = "",
    val availableValues: List<String>? = null,
)

data class TermFilterPropertiesUiState(
    val items: List<FilterableItem> = emptyList(),
    val configurations: Map<FilterableItem, FilterConfiguration> = mapOf(),
    val selectedItem: FilterableItem? = null,
)

data class FilterConfiguration(
    val matchType: MatchType? = null,
    val value: String = "",
) {
    val isEmpty: Boolean
        get() = matchType == null && value.isEmpty()
}