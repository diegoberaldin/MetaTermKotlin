package terms.ui

import data.EntryModel
import data.LanguageModel
import data.SearchCriterion
import data.TermModel
import data.TermbaseModel

data class TermsUiState(
    val currentTermbase: TermbaseModel? = null,
    val terms: List<TermModel> = listOf(),
    val selectedEntry: EntryModel? = null,
    val selectedTerm: TermModel? = null,
    val entryEditMode: Boolean = false,
)

data class TermsToolbarUiState(
    val sourceLanguage: LanguageModel? = null,
    val sourceLanguages: List<LanguageModel> = emptyList(),
    val targetLanguage: LanguageModel? = null,
    val targetLanguages: List<LanguageModel> = emptyList(),
)

data class TermsSearchUiState(
    val searchText: String = "",
    val searchCriteria: List<SearchCriterion> = emptyList(),
)
