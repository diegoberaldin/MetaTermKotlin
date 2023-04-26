package data

sealed interface SearchCriterion {
    data class FuzzyMatch(
        val text: String,
        val matching: List<MatchDescriptor> = emptyList(),
    ) : SearchCriterion

    data class ExactMatch(
        val text: String,
        val matching: List<MatchDescriptor> = emptyList(),
    ) : SearchCriterion

    data class MatchDescriptor(
        val propertyId: Int? = null,
        val lemma: Boolean? = null,
        val lang: String? = null,
    )

    companion object {
        fun getDefault(sourceLang: String): List<SearchCriterion> = listOf(
            FuzzyMatch(
                text = "",
                matching = listOf(MatchDescriptor(lemma = true, lang = sourceLang))
            )
        )
    }
}

fun List<SearchCriterion>.includingSearch(text: String?): List<SearchCriterion> {
    if (text.isNullOrEmpty()) {
        return this
    }

    return map {
        if (it is SearchCriterion.FuzzyMatch && it.text.isEmpty()) {
            it.copy(text = text)
        } else {
            it
        }
    }
}
