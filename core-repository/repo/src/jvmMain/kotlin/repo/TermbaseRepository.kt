package repo

import persistence.dao.TermbaseDAO
import data.TermbaseModel
import kotlinx.coroutines.flow.distinctUntilChanged

class TermbaseRepository(
    private val termbaseDAO: TermbaseDAO,
) {

    val all
        get() = termbaseDAO.observeAll().distinctUntilChanged()

    suspend fun getAll(): List<TermbaseModel> = termbaseDAO.getAll()

    suspend fun getById(id: Int): TermbaseModel? = termbaseDAO.getById(id)

    suspend fun create(model: TermbaseModel): Int = termbaseDAO.create(model)

    suspend fun update(model: TermbaseModel) {
        termbaseDAO.update(model)
    }

    suspend fun delete(model: TermbaseModel) {
        termbaseDAO.delete(model)
    }
}
