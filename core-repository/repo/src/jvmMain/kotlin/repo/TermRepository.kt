package repo

import persistence.dao.TermDAO
import data.SearchCriterion
import data.TermModel

class TermRepository(
    private val termDAO: TermDAO,
) {

    suspend fun getAll(
        termbaseId: Int,
        mainLang: String? = null,
        entryId: Int? = null,
        criteria: List<SearchCriterion> = emptyList(),
    ): List<TermModel> = termDAO.getAll(
        termbaseId = termbaseId,
        mainLang = mainLang,
        entryId = entryId,
        criteria = criteria,
    )

    suspend fun getAll(entryId: Int): List<TermModel> = termDAO.getAll(
        entryId = entryId,
    )

    suspend fun countAll(termbaseId: Int): Long = termDAO.countAll(
        termbaseId = termbaseId,
    )

    suspend fun countByLanguage(code: String, termbaseId: Int): Long = termDAO.countByLanguage(
        code = code,
        termbaseId = termbaseId,
    )

    suspend fun getById(termId: Int): TermModel? = termDAO.getById(termId)

    suspend fun create(model: TermModel): Int = termDAO.create(model)

    suspend fun update(model: TermModel) {
        termDAO.update(model)
    }

    suspend fun delete(model: TermModel) {
        termDAO.delete(model)
    }
}
