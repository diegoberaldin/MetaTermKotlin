package repo

import persistence.dao.LanguagePropertyValueDAO
import data.PropertyValueModel

class LanguagePropertyValueRepository(
    private val languagePropertyValueDAO: LanguagePropertyValueDAO,
) {
    suspend fun getAll(languageId: Int, entryId: Int): List<PropertyValueModel> =
        languagePropertyValueDAO.getAll(languageId = languageId, entryId = entryId)

    suspend fun getById(valueId: Int): PropertyValueModel? = languagePropertyValueDAO.getById(valueId)

    suspend fun create(model: PropertyValueModel, languageId: Int, entryId: Int): Int =
        languagePropertyValueDAO.create(
            model = model,
            languageId = languageId,
            entryId = entryId,
        )

    suspend fun delete(model: PropertyValueModel) {
        languagePropertyValueDAO.delete(model)
    }

    suspend fun update(model: PropertyValueModel) {
        languagePropertyValueDAO.update(model)
    }
}
