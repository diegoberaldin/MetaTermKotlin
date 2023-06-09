package persistence.dao

import MockFileManager
import data.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import persistence.AppDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TermDAOTest {

    private lateinit var appDb: AppDatabase
    private lateinit var sut: TermDAO
    private var termbaseId: Int = 0
    private var entryId: Int = 0

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
        appDb = AppDatabase(
            filename = "test",
            fileManager = MockFileManager
        )
        sut = appDb.termDao()

        val termbaseDAO = appDb.termbaseDao()
        val entryDao = appDb.entryDao()
        val languageDao = appDb.languageDao()
        runBlocking {
            termbaseId = termbaseDAO.create(TermbaseModel(name = "test"))
            entryId = entryDao.create(EntryModel(termbaseId = termbaseId))
            languageDao.create(LanguageModel(code = "en", termbaseId = termbaseId))
            languageDao.create(LanguageModel(code = "it", termbaseId = termbaseId))
        }
    }

    @AfterTest
    fun teardown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenEmptyTermbaseWhenTermCreatedThenRowIsCreated() = runTest {
        val model = TermModel(entryId = entryId, lemma = "test")
        val id = sut.create(model)
        assert(id > 0)
    }

    @Test
    fun givenExistingTermWhenGeyByIdIsCalledThenValueIsReturned() = runTest {
        val model = TermModel(entryId = entryId, lemma = "test")
        val id = sut.create(model)

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.lemma == "test")
    }

    @Test
    fun givenExistingTermWhenIsDeletedThenNoValueIsReturned() = runTest {
        val model = TermModel(entryId = entryId, lemma = "test")
        val id = sut.create(model)
        val old = sut.getById(id)
        assert(old != null)

        sut.delete(model.copy(id = id))

        val res = sut.getById(id)
        assert(res == null)
    }

    @Test
    fun givenExistingTermWhenIsUpdatedThenValueIsReturned() = runTest {
        val model = TermModel(entryId = entryId, lemma = "test")
        val id = sut.create(model)
        val old = sut.getById(id)
        assert(old != null)

        sut.update(model.copy(id = id, lemma = "test 2"))

        val res = sut.getById(id)
        assert(res != null)
        assert(res?.lemma == "test 2")
    }

    @Test
    fun givenExistingTermsWhenGetAllIsCalledThenAllValuesReturned() = runTest {
        val model = TermModel(entryId = entryId, lemma = "test")
        val model2 = TermModel(entryId = entryId, lemma = "test 2")
        sut.create(model)
        sut.create(model2)

        val res = sut.getAll(entryId = entryId)

        assert(res.size == 2)
    }

    @Test
    fun givenExistingTermsWhenCountAllIsCalledThenCorrectResultIsReturned() = runTest {
        val model = TermModel(entryId = entryId, lemma = "test")
        val model2 = TermModel(entryId = entryId, lemma = "test 2")
        sut.create(model)
        sut.create(model2)

        val res = sut.countAll(termbaseId = termbaseId)

        assert(res == 2L)
    }

    @Test
    fun givenExistingTermsWhenCountByLanguageIsCalledThenCorrectResultIsReturned() = runTest {
        val model = TermModel(entryId = entryId, lemma = "test", lang = "en")
        val model2 = TermModel(entryId = entryId, lemma = "test 2", lang = "it")
        sut.create(model)
        sut.create(model2)

        val res = sut.countByLanguage(code = "en", termbaseId = termbaseId)

        assert(res == 1L)
    }

    @Test
    fun givenExistingTermsWhenSearchedWithEmptyCriteriaThenCorrectResultsAreReturned() = runTest {
        val terms = listOf(
            TermModel(entryId = entryId, lemma = "test", lang = "en"),
            TermModel(entryId = entryId, lemma = "test 2", lang = "en"),
            TermModel(entryId = entryId, lemma = "another", lang = "en"),
            TermModel(entryId = entryId, lemma = "another 2", lang = "en"),
            TermModel(entryId = entryId, lemma = "test", lang = "it"),
        )
        for (t in terms) {
            sut.create(t)
        }

        val res = sut.getAll(
            termbaseId = termbaseId,
            mainLang = "en",
        )

        assert(res.size == 4)
    }

    @Test
    fun givenExistingTermsWhenSearchedWithLemmaFuzzyMatchThenCorrectResultsAreReturned() = runTest {
        val terms = listOf(
            TermModel(entryId = entryId, lemma = "test", lang = "en"),
            TermModel(entryId = entryId, lemma = "test 2", lang = "en"),
            TermModel(entryId = entryId, lemma = "another", lang = "en"),
            TermModel(entryId = entryId, lemma = "another 2", lang = "en"),
            TermModel(entryId = entryId, lemma = "test", lang = "it"),
        )
        for (t in terms) {
            sut.create(t)
        }

        val res = sut.getAll(
            termbaseId = termbaseId,
            mainLang = "en",
            criteria = listOf(
                SearchCriterion.FuzzyMatch(
                    text = "test",
                    matching = listOf(SearchCriterion.MatchDescriptor(lemma = true, lang = "en"))
                )
            )
        )

        assert(res.size == 2)
    }

    @Test
    fun givenExistingTermsWhenSearchedWithLemmaExactMatchThenCorrectResultsAreReturned() = runTest {
        val terms = listOf(
            TermModel(entryId = entryId, lemma = "test", lang = "en"),
            TermModel(entryId = entryId, lemma = "test 2", lang = "en"),
            TermModel(entryId = entryId, lemma = "another", lang = "en"),
            TermModel(entryId = entryId, lemma = "another 2", lang = "en"),
            TermModel(entryId = entryId, lemma = "test", lang = "it"),
        )
        for (t in terms) {
            sut.create(t)
        }

        val res = sut.getAll(
            termbaseId = termbaseId,
            mainLang = "en",
            criteria = listOf(
                SearchCriterion.ExactMatch(
                    text = "test",
                    matching = listOf(SearchCriterion.MatchDescriptor(lemma = true, lang = "en"))
                )
            )
        )

        assert(res.size == 1)
    }
}