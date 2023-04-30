package dao

import AppDatabase
import MockFileManager
import data.PropertyLevel
import data.PropertyModel
import data.PropertyType
import data.TermbaseModel
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PropertyDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: PropertyDAO
    private var termbaseId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.propertyDao()

        val termbaseDAO = appDb.termbaseDao()
        runBlocking {
            termbaseId = termbaseDAO.create(TermbaseModel(name = "test"))
        }
    }

    @AfterTest
    fun teardown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenEmptyTermbaseWhenPropertyCreatedThenRowIsCreated() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop")
        runBlocking {
            val id = sut.create(model)
            assert(id > 0)
        }
    }

    @Test
    fun givenExistingEntryLevelPropertyWhenGeyByIdIsCalledThenValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", level = PropertyLevel.ENTRY)
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "prop")
            assert(res?.level == PropertyLevel.ENTRY)
        }
    }

    @Test
    fun givenExistingLanguageLevelPropertyWhenGeyByIdIsCalledThenValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", level = PropertyLevel.LANGUAGE)
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "prop")
            assert(res?.level == PropertyLevel.LANGUAGE)
        }
    }

    @Test
    fun givenExistingTermLevelPropertyWhenGeyByIdIsCalledThenValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", level = PropertyLevel.TERM)
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "prop")
            assert(res?.level == PropertyLevel.TERM)
        }
    }

    @Test
    fun givenExistingTextPropertyWhenGeyByIdIsCalledThenValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", type = PropertyType.TEXT)
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "prop")
            assert(res?.type == PropertyType.TEXT)
        }
    }

    @Test
    fun givenExistingPicklistPropertyWhenGeyByIdIsCalledThenValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", type = PropertyType.PICKLIST)
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "prop")
            assert(res?.type == PropertyType.PICKLIST)
        }
    }

    @Test
    fun givenExistingImagePropertyWhenGeyByIdIsCalledThenValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", type = PropertyType.IMAGE)
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "prop")
            assert(res?.type == PropertyType.IMAGE)
        }
    }

    @Test
    fun givenExistingPropertyWhenPropertyIsDeletedThenNoValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop")
        runBlocking {
            val id = sut.create(model)
            val old = sut.getById(id)
            assert(old != null)

            sut.delete(model.copy(id = id))

            val res = sut.getById(id)
            assert(res == null)
        }
    }

    @Test
    fun givenExistingPropertyWhenPropertyIsUpdatedThenNoValueIsReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop")
        runBlocking {
            val id = sut.create(model)
            val old = sut.getById(id)
            assert(old != null)

            sut.update(model.copy(id = id, name = "prop 2"))

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "prop 2")
        }
    }

    @Test
    fun givenExistingPropertiesWhenGetAllIsCalledThenAllValuesReturned() {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop 1")
        val model2 = PropertyModel(termbaseId = termbaseId, name = "prop 2")
        runBlocking {
            sut.create(model)
            sut.create(model2)

            val res = sut.getAll(termbaseId = termbaseId)

            assert(res.size == 2)
        }
    }
}