package dao

import AppDatabase
import MockFileManager
import data.TermbaseModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TermbaseDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: TermbaseDAO

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.termbaseDao()
    }

    @AfterTest
    fun teardown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenDaoWhenTermbaseCreatedThenRowIsCreated() {
        val model = TermbaseModel(name = "test")
        runBlocking {
            val id = sut.create(model)
            assert(id > 0)
        }
    }

    @Test
    fun givenExistingTermbaseWhenGetByIdIsCalledThenMatchingValueIsReturned() {
        val model = TermbaseModel(name = "test")
        runBlocking {
            val id = sut.create(model)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "test")
        }
    }

    @Test
    fun givenExistingTermbaseWhenUpdatedThenMatchingValueIsReturned() {
        val model = TermbaseModel(name = "test")
        runBlocking {
            val id = sut.create(model)
            val old = sut.getById(id) ?: throw AssertionError()
            val new = old.copy(name = "test 2")

            sut.update(new)

            val res = sut.getById(id)
            assert(res != null)
            assert(res?.name == "test 2")
        }
    }

    @Test
    fun givenExistingTermbasesWhenGetAllIsCalledThenAllValuesAreReturned() {
        val model1 = TermbaseModel(name = "test 1")
        val model2 = TermbaseModel(name = "test 2")
        runBlocking {
            sut.create(model1)
            sut.create(model2)

            val res = sut.getAll()

            assert(res.size == 2)
        }
    }

    @Test
    fun givenExistingTermbasesWhenDeleteCalledThenDbIsRemoved() {
        val model1 = TermbaseModel(name = "test 1")
        val model2 = TermbaseModel(name = "test 2")
        runBlocking {
            val id = sut.create(model1)

            val tb = sut.getById(id) ?: throw AssertionError()
            sut.delete(tb)

            val res = sut.getById(id)
            assert(res == null)
        }
    }

    @Test
    fun givenExistingTermbasesWhenObservedValuesChangeAccordingly() {
        val model1 = TermbaseModel(name = "test 1")
        val model2 = TermbaseModel(name = "test 2")
        runBlocking {
            // when empty
            val flow = sut.observeAll()
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