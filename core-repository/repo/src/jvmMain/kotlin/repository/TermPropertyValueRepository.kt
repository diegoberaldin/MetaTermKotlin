package repository

import dao.TermPropertyValueDAO
import data.PropertyValueModel

class TermPropertyValueRepository(
    private val termPropertyValueDAO: TermPropertyValueDAO,
) {
    suspend fun getAll(termId: Int): List<PropertyValueModel> = termPropertyValueDAO.getAll(termId)

    suspend fun getById(valueId: Int) = termPropertyValueDAO.getById(valueId)

    suspend fun create(model: PropertyValueModel, termId: Int): Int = termPropertyValueDAO.create(
        model = model,
        termId = termId,
    )

    suspend fun delete(model: PropertyValueModel) {
        termPropertyValueDAO.delete(model)
    }

    suspend fun update(model: PropertyValueModel) {
        termPropertyValueDAO.update(model)
    }
}
