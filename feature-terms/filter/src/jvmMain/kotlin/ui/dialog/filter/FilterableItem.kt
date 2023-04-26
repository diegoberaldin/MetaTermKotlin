package ui.dialog.filter

import data.PropertyLevel
import data.PropertyModel

sealed interface FilterableItem {
    data class SectionHeader(
        val level: PropertyLevel,
        val lang: String? = null,
    ) : FilterableItem

    data class LanguageHeader(
        val name: String,
        val lang: String,
    ) : FilterableItem

    data class Lemma(
        val lang: String,
    ) : FilterableItem

    data class Property(
        val property: PropertyModel,
        val lang: String? = null,
    ) : FilterableItem
}
