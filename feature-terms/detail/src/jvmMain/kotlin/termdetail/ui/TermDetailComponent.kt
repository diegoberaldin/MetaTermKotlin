package termdetail.ui

import data.LanguageModel
import data.SearchCriterion
import kotlinx.coroutines.flow.StateFlow

interface TermDetailComponent {
    val uiState: StateFlow<TermDetailUiState>
    val availablePropertiesUiState: StateFlow<TermDetailAvailablePropertiesUiState>
    fun setEditMode(value: Boolean)
    fun load(
        entryId: Int?,
        termbaseId: Int?,
        newEntry: Boolean = false,
        searchCriteria: List<SearchCriterion> = emptyList(),
    )

    fun setLanguages(languages: List<LanguageModel>)
    fun calculateAvailableEntryProperties()
    fun calculateAvailableLanguageProperties(index: Int)
    fun calculateAvailableTermProperties(index: Int)
    fun save()
    fun startInsertTermAt(index: Int, langCode: String)
    fun markTermFormDeletion(index: Int)
    fun setTermLemma(index: Int, lemma: String)
    fun startInsertPropertyAt(
        index: Int,
        valueId: Int,
        propertyId: Int,
        entryId: Int?,
        languageId: Int?,
        termId: Int?,
        new: Boolean = false,
    )

    fun markPropertyForDeletion(index: Int)
    fun setPropertyValue(value: String, index: Int)
}