package repo

import MockFileManager
import data.LanguageModel
import data.TermbaseModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import persistence.AppDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test

class LanguageRepositoryTest {

    private lateinit var sut: LanguageRepository
    private var termbaseId = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()

        val appDb = AppDatabase(filename = "test", fileManager = MockFileManager)
        val termbaseDao = appDb.termbaseDao()
        runBlocking {
            termbaseId = termbaseDao.create(TermbaseModel(name = "test"))
        }
        val languageDao = appDb.languageDao()
        sut = LanguageRepository(languageDAO = languageDao)
    }

    @Test
    fun givenRepositoryWhenLanguageCreatedThenRowInInserted() = runTest {
        val id = sut.create(LanguageModel(termbaseId = termbaseId, code = "en"))

        assert(id > 0)
    }

    @Test
    fun givenExistingLanguageWhenGetByIdIsCalledThenCorrectDataIsReturned() = runTest {
        val id = sut.create(LanguageModel(termbaseId = termbaseId, code = "en"))

        val res = sut.getById(id)

        assert(res != null)
        assert(res?.code == "en")
    }

    @Test
    fun givenNonExistingLanguageWhenGetByIdIsCalledThenNoDataIsReturned() = runTest {
        val res = sut.getById(1)

        assert(res == null)
    }

    @Test
    fun givenExistingLanguageWhenGetByCodeIsCalledThenCorrectDataIsReturned() = runTest {
        sut.create(LanguageModel(termbaseId = termbaseId, code = "en"))

        val res = sut.getByCode(code = "en", termbaseId = termbaseId)

        assert(res != null)
        assert(res?.code == "en")
    }

    @Test
    fun givenNonExistingLanguageWhenGetByCodeIsCalledThenNoDataIsReturned() = runTest {
        val res = sut.getByCode(code = "en", termbaseId = termbaseId)

        assert(res == null)
    }

    @Test
    fun givenNonExistingLanguageWhenGetAllCalledThenCorrectDataIsReturned() = runTest {
        val res = sut.getAll(termbaseId = termbaseId)

        assert(res.isEmpty())
    }

    @Test
    fun givenExistingLanguageWhenGetAllCalledThenCorrectDataIsReturned() = runTest {
        sut.create(LanguageModel(termbaseId = termbaseId, code = "en"))

        val res = sut.getAll(termbaseId = termbaseId)

        assert(res.size == 1)
        assert(res.first().code == "en")
    }

    @Test
    fun givenExistingLanguageWhenDeletedThenRowDoesNotExistAnyMore() = runTest {
        val model = LanguageModel(termbaseId = termbaseId, code = "en")
        val id = sut.create(model)
        val old = sut.getAll(termbaseId)
        assert(old.isNotEmpty())

        sut.delete(model.copy(id = id))

        val res = sut.getAll(termbaseId = termbaseId)
        assert(res.isEmpty())
    }

    @Test
    fun givenRepositoryWhenDefaultLanguagesCalledThenAtLeastALanguageIsReturned() {
        val res = sut.getDefaultLanguages()

        assert(res.isNotEmpty())
        assert(res.any { it.code == "en" })
    }
}