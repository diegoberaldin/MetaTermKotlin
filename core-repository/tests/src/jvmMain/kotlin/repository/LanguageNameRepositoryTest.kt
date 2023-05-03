package repository

import L10n
import kotlin.test.Test

class LanguageNameRepositoryTest {

    private val sut = LanguageNameRepository()

    init {
        L10n.setLanguage("en")
    }

    @Test
    fun givenRepositoryWhenItLanguageFlagIsQueriedThenCorrectNameIsReturned() {
        val res = sut.getName("it")
        assert(res == "Italiano")
    }

    @Test
    fun givenRepositoryWhenDeLanguageFlagIsQueriedThenCorrectNameIsReturned() {
        val res = sut.getName("de")
        assert(res == "Deutsch")
    }

    @Test
    fun givenRepositoryWhenFrLanguageFlagIsQueriedThenCorrectNameIsReturned() {
        val res = sut.getName("fr")
        assert(res == "Français")
    }

    @Test
    fun givenRepositoryWhenElLanguageFlagIsQueriedThenCorrectNameIsReturned() {
        val res = sut.getName("el")
        assert(res == "Ελληνικά")
    }

    @Test
    fun givenRepositoryWhenUnknownLanguageFlagIsQueriedThenDefaultIsReturned() {
        val res = sut.getName("xx")
        assert(res == "English")
    }
}