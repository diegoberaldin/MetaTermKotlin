package repo

import persistence.dao.EntryPropertyValueDAO
import data.PropertyValueModel

class EntryPropertyValueRepository(
    private val entryPropertyValueDAO: EntryPropertyValueDAO,
) {
    suspend fun getAll(entryId: Int): List<PropertyValueModel> = entryPropertyValueDAO.getAll(entryId)

    suspend fun getById(valueId: Int): PropertyValueModel? = entryPropertyValueDAO.getById(valueId)

    suspend fun create(model: PropertyValueModel, entryId: Int): Int = entryPropertyValueDAO.create(
        model = model,
        entryId = entryId,
    )

    suspend fun delete(model: PropertyValueModel) {
        entryPropertyValueDAO.delete(model)
    }

    suspend fun update(model: PropertyValueModel) {
        entryPropertyValueDAO.update(model)
    }
}
