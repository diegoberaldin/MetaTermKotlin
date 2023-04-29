package usecase

import data.SearchCriterion
import data.TermModel
import repository.TermRepository

class SearchTermsUseCase(
    private val termRepository: TermRepository,
) {

    suspend operator fun invoke(
        termbaseId: Int,
        mainLang: String,
        criteria: List<SearchCriterion> = emptyList(),
    ): List<TermModel> {
        return termRepository.getAll(
            termbaseId = termbaseId,
            mainLang = mainLang,
            criteria = criteria,
        )
    }
}
