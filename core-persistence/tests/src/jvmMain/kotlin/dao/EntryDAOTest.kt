package dao

import AppDatabase
import MockFileManager
import data.EntryModel
import data.TermbaseModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EntryDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: EntryDAO
    private var termbaseId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.entryDao()

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
    fun givenEmptyTermbaseWhenEntryCreatedThenRowIsCreated() {
        val model = EntryModel(termbaseId = termbaseId)
        runBlocking {
            val id = sut.create(model)
            assert(id > 0)
        }
    }

    @Test
    fun givenExistingEntryWhenEntryGeyByIdIsCalledThenValueIsReturned() {
        val model = EntryModel(termbaseId = termbaseId)
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
        }
    }

    @Test
    fun givenExistingEntryWhenEntryIsDeletedThenNoValueIsReturned() {
        val model = EntryModel(termbaseId = termbaseId)
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
    fun givenExistingEntriesWhenGetAllIsCalledThenAllValuesReturned() {
        val model = EntryModel(termbaseId = termbaseId)
        val model2 = EntryModel(termbaseId = termbaseId)
        runBlocking {
            sut.create(model)
            sut.create(model2)

            val res = sut.getAll(termbaseId = termbaseId)

            assert(res.size == 2)
        }
    }

    @Test
    fun givenExistingEntriesWhenCountAllIsCalledThenCorrectResultIsReturned() {
        val model = EntryModel(termbaseId = termbaseId)
        val model2 = EntryModel(termbaseId = termbaseId)
        runBlocking {
            sut.create(model)
            sut.create(model2)

            val res = sut.countAll(termbaseId = termbaseId)

            assert(res == 2L)
        }
    }

    @Test
    fun givenExistingIncompleteEntriesWhenCountCompleteIsCalledThenCorrectResultIsReturned() {
        val model = EntryModel(termbaseId = termbaseId)
        val model2 = EntryModel(termbaseId = termbaseId)
        runBlocking {
            sut.create(model)
            sut.create(model2)

            val res = sut.countComplete(code = "en", termbaseId = termbaseId)

            assert(res == 0L)
        }
    }

    @Test
    fun givenExistingEntriesWhenObservedValuesChangeAccordingly() {
        val model1 = EntryModel(termbaseId = termbaseId)
        val model2 = EntryModel(termbaseId = termbaseId)
        runBlocking {
            // when empty
            val flow = sut.observeAll(termbaseId = termbaseId)
            val l0 = flow.first()
            assert(l0.isEmpty())

            // after 1 insertion
            sut.create(model1)
            val l1 = flow.first()
            assert(l1.size == 1)

            // after 2 insertions
            val id = sut.create(model2)
            val l2 = flow.first()
            assert(l2.size == 2)

            // after 1 deletion
            sut.delete(model1.copy(id = id))
            val l3 = flow.first()
            assert(l3.size == 1)
        }
    }
}