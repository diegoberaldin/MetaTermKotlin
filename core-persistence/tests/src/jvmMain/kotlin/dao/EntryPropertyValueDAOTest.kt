package dao

import AppDatabase
import MockFileManager
import data.EntryModel
import data.PropertyLevel
import data.PropertyModel
import data.PropertyValueModel
import data.TermbaseModel
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EntryPropertyValueDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: EntryPropertyValueDAO
    private var propertyId: Int = 0
    private var entryId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.entryPropertyValueDao()

        val termbaseDAO = appDb.termbaseDao()
        val propertyDAO = appDb.propertyDao()
        val entryDAO = appDb.entryDao()
        runBlocking {
            val termbaseId = termbaseDAO.create(TermbaseModel(name = "test"))
            propertyId =
                propertyDAO.create(PropertyModel(name = "test", termbaseId = termbaseId, level = PropertyLevel.ENTRY))
            entryId = entryDAO.create(EntryModel(termbaseId = termbaseId))
        }
    }

    @AfterTest
    fun teardown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenEmptyTermbaseWhenPropertyValueIsCreatedThenRowIsCreated() {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        runBlocking {
            val id = sut.create(model = model, entryId = entryId)
            assert(id > 0)
        }
    }

    @Test
    fun givenExistingPropertyValueWhenGetByIdIsCalledThenValueIsReturned() {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        runBlocking {
            val id = sut.create(model = model, entryId = entryId)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.value == "test")
        }
    }

    @Test
    fun givenExistingPropertyValuesWhenGetAllIsCalledThenValueIsReturned() {
        val values = listOf(
            PropertyValueModel(propertyId = propertyId, value = "test 1"),
            PropertyValueModel(propertyId = propertyId, value = "test 2"),
            PropertyValueModel(propertyId = propertyId, value = "test 3"),
        )
        runBlocking {
            for (v in values) {
                sut.create(model = v, entryId = entryId)
            }

            val res = sut.getAll(entryId)
            assert(res.size == values.size)
        }
    }

    @Test
    fun givenExistingPropertyValueWhenIsDeletedThenNoValueIsReturned() {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        runBlocking {
            val id = sut.create(model = model, entryId = entryId)
            val old = sut.getById(id)
            assert(old != null)

            sut.delete(model.copy(id = id))

            val res = sut.getById(id)
            assert(res == null)
        }
    }

    @Test
    fun givenExistingPropertyValueWhenIsUpdatedThenNoValueIsReturned() {
        val model = PropertyValueModel(propertyId = propertyId, value = "test")
        runBlocking {
            val id = sut.create(model = model, entryId = entryId)
            val old = sut.getById(id)
            assert(old != null)

            sut.update(model.copy(id = id, value = "test 2"))

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.value == "test 2")
        }
    }
}