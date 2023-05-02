package dao

import AppDatabase
import MockFileManager
import data.EntryModel
import data.PropertyLevel
import data.PropertyModel
import data.PropertyValueModel
import data.TermModel
import data.TermbaseModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TermPropertyValueDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: TermPropertyValueDAO
    private var propertyId: Int = 0
    private var termId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.termPropertyValueDao()

        val termbaseDAO = appDb.termbaseDao()
        val propertyDAO = appDb.propertyDao()
        val entryDAO = appDb.entryDao()
        val termDAO = appDb.termDao()
        runBlocking {
            val termbaseId = termbaseDAO.create(TermbaseModel(name = "test"))
            propertyId =
                propertyDAO.create(
                    PropertyModel(
                        name = "test",
                        termbaseId = termbaseId,
                        level = PropertyLevel.LANGUAGE
                    )
                )
            val entryId = entryDAO.create(EntryModel(termbaseId = termbaseId))
            termId = termDAO.create(TermModel(entryId = entryId, lemma = "test", lang = "en"))
        }
    }

    @AfterTest
    fun teardown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenEmptyTermbaseWhenPropertyValueIsCreatedThenRowIsCreated() = runTest {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        val id = sut.create(model = model, termId = termId)
        assert(id > 0)
    }

    @Test
    fun givenExistingPropertyValueWhenGetByIdIsCalledThenValueIsReturned() = runTest {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        val id = sut.create(model = model, termId = termId)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.value == "test")
    }

    @Test
    fun givenExistingPropertyValuesWhenGetAllIsCalledThenValueIsReturned() = runTest {
        val values = listOf(
            PropertyValueModel(propertyId = propertyId, value = "test 1"),
            PropertyValueModel(propertyId = propertyId, value = "test 2"),
            PropertyValueModel(propertyId = propertyId, value = "test 3"),
        )
        for (v in values) {
            sut.create(model = v, termId = termId)
        }

        val res = sut.getAll(termId = termId)
        assert(res.size == values.size)
    }

    @Test
    fun givenExistingPropertyValueWhenIsDeletedThenNoValueIsReturned() = runTest {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        val id = sut.create(model = model, termId = termId)
        val old = sut.getById(id)
        assert(old != null)

        sut.delete(model.copy(id = id))

        val res = sut.getById(id)
        assert(res == null)
    }

    @Test
    fun givenExistingPropertyValueWhenIsUpdatedThenNoValueIsReturned() = runTest {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        val id = sut.create(model = model, termId = termId)
        val old = sut.getById(id)
        assert(old != null)

        sut.update(model.copy(id = id, value = "test 2"))

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.value == "test 2")
    }
}