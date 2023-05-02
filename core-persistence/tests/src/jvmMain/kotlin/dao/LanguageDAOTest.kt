package dao

import AppDatabase
import MockFileManager
import data.LanguageModel
import data.TermbaseModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: LanguageDAO
    private var termbaseId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.languageDao()

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
    fun givenEmptyTermbaseWhenLanguageCreatedThenRowIsCreated() = runTest {
        val model = LanguageModel(termbaseId = termbaseId, code = "en")
        val id = sut.create(model)
        assert(id > 0)
    }

    @Test
    fun givenExistingLanguageWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = LanguageModel(termbaseId = termbaseId, code = "en")
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.code == "en")
    }

    @Test
    fun givenExistingLanguageWhenGeyByCodeIsCalledThenValueIsReturned() = runTest {
        val model = LanguageModel(termbaseId = termbaseId, code = "en")
        sut.create(model)

        val res = sut.getByCode(code = "en", termbaseId = termbaseId)
        assert(res != null)
        assert(res?.code == "en")
    }

    @Test
    fun givenNonExistingLanguageWhenGeyByCodeIsCalledThenValueIsReturned() = runTest {
        val model = LanguageModel(termbaseId = termbaseId, code = "en")
        sut.create(model)

        val res = sut.getByCode(code = "it", termbaseId = termbaseId)
        assert(res == null)
    }

    @Test
    fun givenExistingLanguageWhenLanguageIsDeletedThenNoValueIsReturned() = runTest {
        val model = LanguageModel(termbaseId = termbaseId, code = "en")
        val id = sut.create(model)
        val old = sut.getById(id)
        assert(old != null)

        sut.delete(model.copy(id = id))

        val res = sut.getById(id)
        assert(res == null)
    }

    @Test
    fun givenExistingLanguagesWhenGetAllIsCalledThenAllValuesReturned() = runTest {
        val model = LanguageModel(termbaseId = termbaseId, code = "en")
        val model2 = LanguageModel(termbaseId = termbaseId, code = "it")
        sut.create(model)
        sut.create(model2)

        val res = sut.getAll(termbaseId = termbaseId)

        assert(res.size == 2)
    }
}