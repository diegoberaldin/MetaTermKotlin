package repo

import persistence.dao.EntryDAO
import data.EntryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class EntryRepository(
    private val entryDAO: persistence.dao.EntryDAO,
) {

    private val _currentEntry = MutableStateFlow<EntryModel?>(null)
    val currentEntry = _currentEntry.asStateFlow()
    private val _editMode = MutableStateFlow(false)
    val editMode = _editMode.asStateFlow()

    suspend fun create(model: EntryModel): Int = entryDAO.create(model = model)

    suspend fun getAll(termbaseId: Int): List<EntryModel> = entryDAO.getAll(termbaseId = termbaseId)

    suspend fun countAll(termbaseId: Int): Long = entryDAO.countAll(termbaseId = termbaseId)

    suspend fun countComplete(code: String, termbaseId: Int): Long =
        entryDAO.countComplete(code = code, termbaseId = termbaseId)

    suspend fun delete(entry: EntryModel) {
        entryDAO.delete(entry)
    }

    fun observeEntries(termbaseId: Int): Flow<List<EntryModel>> = entryDAO.observeAll(termbaseId).distinctUntilChanged()

    suspend fun getById(id: Int): EntryModel? = entryDAO.getById(entryId = id)

    fun setCurrentEntry(entry: EntryModel?) {
        _currentEntry.value = entry
    }

    fun setEditMode(value: Boolean) {
        _editMode.value = value
    }
}
