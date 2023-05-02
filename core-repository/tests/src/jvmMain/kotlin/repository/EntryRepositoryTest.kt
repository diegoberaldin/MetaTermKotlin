package repository

import AppDatabase
import MockFileManager
import data.EntryModel
import data.TermbaseModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class EntryRepositoryTest {

    private lateinit var sut: EntryRepository
    private var termbaseId = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()

        val appDb = AppDatabase(filename = "test", fileManager = MockFileManager)
        val termbaseDao = appDb.termbaseDao()
        runBlocking {
            termbaseId = termbaseDao.create(TermbaseModel(name = "test"))
        }
        val entryDao = appDb.entryDao()
        sut = EntryRepository(entryDAO = entryDao)
    }

    @AfterTest
    fun tearDown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenRepositoryEmptyWhenGetAllInvokedThenAllEntriesAreReturned() {
        runBlocking {
            val res = sut.getAll(termbaseId)
            assert(res.isEmpty())
        }
    }

    @Test
    fun givenRepositoryWhenCreateAndGetAllInvokedThenAllEntriesAreReturned() {
        runBlocking {
            sut.create(EntryModel(termbaseId = termbaseId))

            val res = sut.getAll(termbaseId)
            assert(res.size == 1)
        }
    }

    @Test
    fun givenRepositoryWhenCreateThenRowIsInserted() {
        runBlocking {
            val id = sut.create(EntryModel(termbaseId = termbaseId))
            assert(id > 0)
        }
    }

    @Test
    fun givenRepositoryWhenCountAllInvokedThenCorrectNumberIsReturned() {
        runBlocking {
            sut.create(EntryModel(termbaseId = termbaseId))
            sut.create(EntryModel(termbaseId = termbaseId))

            val res = sut.countAll(termbaseId = termbaseId)
            assert(res == 2L)
        }
    }

    @Test
    fun givenRepositoryWhenGetByIdInvokedThenCorrectEntryIsReturned() {
        runBlocking {
            val id = sut.create(EntryModel(termbaseId = termbaseId))

            val res = sut.getById(id)

            assert(res != null)
        }
    }

    @Test
    fun givenRepositoryWhenDeleteThenNoEntryCanBeFound() {
        runBlocking {
            val id = sut.create(EntryModel(termbaseId = termbaseId))

            sut.delete((EntryModel(id = id, termbaseId = termbaseId)))

            val res = sut.getById(id)
            assert(res == null)
        }
    }

    @Test()
    fun givenRepositoryWhenSetCurrentEntryThenStateIsUpdated() {
        val model = EntryModel(termbaseId = termbaseId, id = 1)
        sut.setCurrentEntry(model)
        val res = sut.currentEntry.value
        assert(res == model)
    }

    @Test()
    fun givenRepositoryWhenSetEditModeThenStateIsUpdated() {
        val res0 = sut.editMode.value
        assert(!res0)

        sut.setEditMode(true)

        val res1 = sut.editMode.value
        assert(res1)
    }

    @Test
    fun givenRepositoryWhenObservingEntriesThenValuesAreEmitted() {
        runBlocking {
            withTimeout(5.seconds) {
                val flow = sut.observeEntries(termbaseId = termbaseId)
                val list0 = flow.first()
                assert(list0.isEmpty())

                sut.create(EntryModel(termbaseId = termbaseId))
                val list1 = flow.first()
                assert(list1.size == 1)

                val id = sut.create(EntryModel(termbaseId = termbaseId))
                val list2 = flow.first()
                assert(list2.size == 2)

                sut.delete(EntryModel(termbaseId = termbaseId, id = id))
                val list3 = flow.first()
                assert(list3.size == 1)
            }
        }
    }
}