package persistence.dao

import MockFileManager
import data.PicklistValueModel
import data.PropertyModel
import data.PropertyType
import data.TermbaseModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import persistence.AppDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PicklistValueDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: PicklistValueDAO
    private var propertyId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.picklistValueDao()

        val termbaseDAO = appDb.termbaseDao()
        val propertyDAO = appDb.propertyDao()
        runBlocking {
            val termbaseId = termbaseDAO.create(TermbaseModel(name = "test"))
            propertyId = propertyDAO.create(PropertyModel(termbaseId = termbaseId, type = PropertyType.PICKLIST))
        }
    }

    @AfterTest
    fun teardown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenEmptyTermbaseWhenPicklistValuesCreatedThenRowsAreCreated() = runTest {
        val values = listOf(
            PicklistValueModel(propertyId = propertyId, value = "first"),
            PicklistValueModel(propertyId = propertyId, value = "second"),
            PicklistValueModel(propertyId = propertyId, value = "third"),
        )
        val res = sut.insertAll(values = values, propertyId = propertyId)
        assert(res.size == values.size)
    }

    @Test
    fun givenExistingPicklistValuesWhenGetAllIsCalledThenCorrectValuesAreReturned() = runTest {
        val values = listOf(
            PicklistValueModel(propertyId = propertyId, value = "first"),
            PicklistValueModel(propertyId = propertyId, value = "second"),
            PicklistValueModel(propertyId = propertyId, value = "third"),
        )
        sut.insertAll(values = values, propertyId = propertyId)

        val res = sut.getAll(propertyId = propertyId)
        assert(res.size == values.size)
        assert(res.minByOrNull { it.value }?.value == "first")
    }

    @Test
    fun givenExistingPicklistValuesWhenAreDeletedThenNoValueIsReturned() = runTest {
        val values = listOf(
            PicklistValueModel(propertyId = propertyId, value = "first"),
            PicklistValueModel(propertyId = propertyId, value = "second"),
            PicklistValueModel(propertyId = propertyId, value = "third"),
        )
        sut.insertAll(values = values, propertyId = propertyId)
        val oldRes = sut.getAll(propertyId = propertyId)
        assert(oldRes.size == values.size)

        sut.deleteAll(propertyId = propertyId)
        val res = sut.getAll(propertyId = propertyId)
        assert(res.isEmpty())
    }
}