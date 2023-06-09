package termdetail.ui

import data.LanguageModel
import data.PropertyModel
import data.PropertyType

sealed interface TermDetailItem {
    data class EntryId(val id: Int) : TermDetailItem
    data class CreateTermButton(val langCode: String) : TermDetailItem

    data class EditTermField(val lemma: String, val termId: Int, val langCode: String) : TermDetailItem
    data class LanguageTitle(
        val language: LanguageModel,
        val flag: String = "",
    ) : TermDetailItem

    data class Term(val lemma: String, val termId: Int) : TermDetailItem
    data class Property(
        val id: Int?,
        val propertyId: Int,
        val name: String,
        val value: String,
        val termId: Int? = null,
        val languageId: Int? = null,
        val entryId: Int? = null,
        val type: PropertyType? = null,
    ) : TermDetailItem

    data class CreatePropertyButton(
        val termId: Int? = null,
        val languageId: Int? = null,
        val entryId: Int? = null,
    ) : TermDetailItem

    data class EditPropertyField(
        val name: String,
        val value: String,
        val propertyId: Int,
        val valueId: Int,
        val picklistValues: List<String> = emptyList(),
        val entryId: Int? = null,
        val languageId: Int? = null,
        val termId: Int? = null,
        val type: PropertyType? = null,
    ) : TermDetailItem
}

data class TermDetailUiState(
    val items: List<TermDetailItem> = emptyList(),
    val loading: Boolean = false,
)

data class TermDetailAvailablePropertiesUiState(
    val entryLevelProperties: List<PropertyModel> = emptyList(),
    val languageLevelProperties: List<PropertyModel> = emptyList(),
    val termLevelProperties: List<PropertyModel> = emptyList(),
)
