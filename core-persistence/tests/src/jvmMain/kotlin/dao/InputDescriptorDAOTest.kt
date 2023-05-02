package dao

import AppDatabase
import MockFileManager
import data.InputDescriptorModel
import data.TermbaseModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InputDescriptorDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: InputDescriptorDAO
    private var termbaseId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.inputDescriptorDao()

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
    fun givenEmptyTermbaseWhenEntryCreatedThenRowIsCreated() = runTest {
        val model = InputDescriptorModel(termbaseId = termbaseId)
        val id = sut.create(model)
        assert(id > 0)
    }

    @Test
    fun givenExistingEntryWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = InputDescriptorModel(termbaseId = termbaseId, lemma = true)
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.lemma == true)
    }

    @Test
    fun givenExistingEntryWhenIsDeletedThenNoValueIsReturned() = runTest {
        val model = InputDescriptorModel(termbaseId = termbaseId)
        val id = sut.create(model)
        val old = sut.getById(id)
        assert(old != null)

        sut.delete(model.copy(id = id))

        val res = sut.getById(id)
        assert(res == null)
    }

    @Test
    fun givenExistingEntryWhenIsUpdatedThenNoValueIsReturned() = runTest {
        val model = InputDescriptorModel(termbaseId = termbaseId, lemma = false)
        val id = sut.create(model)
        val old = sut.getById(id)
        assert(old != null)
        assert(old?.lemma == false)

        sut.update(model.copy(id = id, lemma = true))

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.lemma == true)
    }

    @Test
    fun givenExistingEntriesWhenGetAllIsCalledThenAllValuesReturned() = runTest {
        val values = listOf(
            InputDescriptorModel(termbaseId = termbaseId),
            InputDescriptorModel(termbaseId = termbaseId),
        )
        for (v in values) {
            sut.create(v)
        }

        val res = sut.getAll(termbaseId = termbaseId)

        assert(res.size == 2)
    }
}