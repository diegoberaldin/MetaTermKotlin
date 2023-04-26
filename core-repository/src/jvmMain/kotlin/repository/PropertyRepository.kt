package repository

import dao.PicklistValueDAO
import dao.PropertyDAO
import data.PropertyModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PropertyRepository(
    private val propertyDAO: PropertyDAO,
    private val picklistValueDAO: PicklistValueDAO,
) {

    private val _editMode = MutableStateFlow(false)
    val editMode = _editMode.asStateFlow()

    suspend fun getAll(termbaseId: Int): List<PropertyModel> = propertyDAO.getAll(termbaseId).map {
        val propertyId = it.id
        val picklistValues = picklistValueDAO.getAll(propertyId)
        it.copy(picklistValues = picklistValues)
    }

    suspend fun getById(id: Int): PropertyModel? = propertyDAO.getById(id)?.let {
        val propertyId = it.id
        val picklistValues = picklistValueDAO.getAll(propertyId)
        it.copy(picklistValues = picklistValues)
    }

    suspend fun create(model: PropertyModel): Int {
        val propertyId = propertyDAO.create(model)
        picklistValueDAO.insertAll(model.picklistValues, propertyId)
        return propertyId
    }

    suspend fun update(model: PropertyModel) {
        val propertyId = model.id
        picklistValueDAO.deleteAll(propertyId)
        propertyDAO.update(model)
        picklistValueDAO.insertAll(model.picklistValues, propertyId)
    }

    suspend fun delete(model: PropertyModel) {
        val propertyId = model.id
        picklistValueDAO.deleteAll(propertyId)
        propertyDAO.delete(model)
    }
}
