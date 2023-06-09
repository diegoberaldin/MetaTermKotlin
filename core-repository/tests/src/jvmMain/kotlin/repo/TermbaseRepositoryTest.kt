package repo

import MockFileManager
import data.TermbaseModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import persistence.AppDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class TermbaseRepositoryTest {

    private lateinit var sut: TermbaseRepository

    @BeforeTest
    fun setup() {
        MockFileManager.setup()

        val appDb = AppDatabase(filename = "test", fileManager = MockFileManager)
        val termbaseDao = appDb.termbaseDao()
        sut = TermbaseRepository(termbaseDAO = termbaseDao)
    }

    @AfterTest
    fun tearDown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenEmptyRepositoryWhenGetAllInvokedThenAllTermbasesAreReturned() = runTest {
        val res = sut.getAll()
        assert(res.isEmpty())
    }

    @Test
    fun givenRepositoryWhenCreateAndGetAllInvokedThenAllTermbasesAreReturned() = runTest {
        sut.create(TermbaseModel(name = "test tb"))

        val res = sut.getAll()
        assert(res.size == 1)
        assert(res.first().name == "test tb")
    }

    @Test
    fun givenRepositoryWhenCreateThenItemIsCreated() = runTest {
        val res = sut.create(TermbaseModel(name = "test tb"))

        assert(res > 0L)
    }

    @Test
    fun givenExistingTermbaseWhenGetByIdInvokedThenCorrectDataIsReturned() = runTest {
        val id = sut.create(TermbaseModel(name = "test tb"))

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.name == "test tb")
    }

    @Test
    fun givenExistingTermbaseWhenUpdatedThenDataIsChangedAccordingly() = runTest {
        val model = TermbaseModel(name = "test tb")
        val id = sut.create(model)

        sut.update(model.copy(id = id, name = "test updated"))

        val res = sut.getById(id)
        assert(res?.name == "test updated")
    }

    @Test
    fun givenExistingTermbaseWhenDeletedThenNoDataIsRetrieved() = runTest {
        val model = TermbaseModel(name = "test tb")
        val id = sut.create(model)

        sut.delete(model.copy(id = id))

        val res = sut.getById(id)
        assert(res == null)
    }

    @Test
    fun givenRepositoryWhenObservingAllTermbasesThenCorrectDataIsEmitted() = runTest {
        withTimeout(5.seconds) {
            val flow = sut.all
            val list0 = flow.first()
            assert(list0.isEmpty())

            sut.create(TermbaseModel(name = "test 1"))
            val list1 = flow.first()
            assert(list1.size == 1)

            val id = sut.create(TermbaseModel(name = "test 2"))
            val list2 = flow.first()
            assert(list2.size == 2)

            sut.delete(TermbaseModel(name = "test 2", id = id))
            val list3 = flow.first()
            assert(list3.size == 1)
        }
    }
}