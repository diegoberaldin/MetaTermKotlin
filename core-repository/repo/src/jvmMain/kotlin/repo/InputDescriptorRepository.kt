package repo

import persistence.dao.InputDescriptorDAO
import data.InputDescriptorModel

class InputDescriptorRepository(
    private val inputDescriptorDAO: InputDescriptorDAO,
) {

    suspend fun create(model: InputDescriptorModel): Int = inputDescriptorDAO.create(model)

    suspend fun update(model: InputDescriptorModel) {
        inputDescriptorDAO.update(model)
    }

    suspend fun delete(model: InputDescriptorModel) {
        inputDescriptorDAO.delete(model)
    }

    suspend fun getAll(termbaseId: Int): List<InputDescriptorModel> =
        inputDescriptorDAO.getAll(termbaseId = termbaseId)

    suspend fun getById(id: Int): InputDescriptorModel? = inputDescriptorDAO.getById(id)
}
