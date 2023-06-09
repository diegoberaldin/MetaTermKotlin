package dialogfilter.ui

import data.LanguageModel
import data.SearchCriterion
import data.TermbaseModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface TermFilterComponent {
    val done: SharedFlow<List<SearchCriterion>>
    val propertiesUiState: StateFlow<TermFilterPropertiesUiState>
    val uiState: StateFlow<TermFilterUiState>
    fun loadInitial(termbase: TermbaseModel, criteria: List<SearchCriterion>, sourceLanguage: LanguageModel?)
    fun toggleSelection(item: FilterableItem)
    fun setCurrentMatchType(value: MatchType)
    fun setCurrentValue(value: String)
    fun clearCurrent()
    fun clearAll()
    fun submit()
}