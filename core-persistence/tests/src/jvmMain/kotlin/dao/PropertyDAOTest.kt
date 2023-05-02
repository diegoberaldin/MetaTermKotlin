package dao

import AppDatabase
import MockFileManager
import data.PropertyLevel
import data.PropertyModel
import data.PropertyType
import data.TermbaseModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun givenEmptyTermbaseWhenPropertyCreatedThenRowIsCreated() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop")
        val id = sut.create(model)
        assert(id > 0)
    }

    @Test
    fun givenExistingEntryLevelPropertyWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", level = PropertyLevel.ENTRY)
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "prop")
        assert(res?.level == PropertyLevel.ENTRY)
    }

    @Test
    fun givenExistingLanguageLevelPropertyWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", level = PropertyLevel.LANGUAGE)
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "prop")
        assert(res?.level == PropertyLevel.LANGUAGE)
    }

    @Test
    fun givenExistingTermLevelPropertyWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", level = PropertyLevel.TERM)
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "prop")
        assert(res?.level == PropertyLevel.TERM)
    }

    @Test
    fun givenExistingTextPropertyWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", type = PropertyType.TEXT)
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "prop")
        assert(res?.type == PropertyType.TEXT)
    }

    @Test
    fun givenExistingPicklistPropertyWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", type = PropertyType.PICKLIST)
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "prop")
        assert(res?.type == PropertyType.PICKLIST)
    }

    @Test
    fun givenExistingImagePropertyWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop", type = PropertyType.IMAGE)
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "prop")
        assert(res?.type == PropertyType.IMAGE)
    }

    @Test
    fun givenExistingPropertyWhenPropertyIsDeletedThenNoValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop")
        val id = sut.create(model)
        val old = sut.getById(id)
        assert(old != null)

        sut.delete(model.copy(id = id))

        val res = sut.getById(id)
        assert(res == null)
    }

    @Test
    fun givenExistingPropertyWhenPropertyIsUpdatedThenNoValueIsReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop")
        val id = sut.create(model)
        val old = sut.getById(id)
        assert(old != null)

        sut.update(model.copy(id = id, name = "prop 2"))

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "prop 2")
    }

    @Test
    fun givenExistingPropertiesWhenGetAllIsCalledThenAllValuesReturned() = runTest {
        val model = PropertyModel(termbaseId = termbaseId, name = "prop 1")
        val model2 = PropertyModel(termbaseId = termbaseId, name = "prop 2")
        sut.create(model)
        sut.create(model2)

        val res = sut.getAll(termbaseId = termbaseId)

        assert(res.size == 2)
    }
}