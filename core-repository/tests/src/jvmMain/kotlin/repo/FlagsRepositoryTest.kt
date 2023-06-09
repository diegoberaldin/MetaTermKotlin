package repo

import kotlin.test.Test

class FlagsRepositoryTest {

    private val sut = FlagsRepository()

    @Test
    fun givenRepositoryWhenItLanguageFlagIsQueriedThenCorrectFlagIsReturned() {
        val res = sut.getFlag("it")
        assert(res == "\uD83C\uDDEE\uD83C\uDDF9")
    }

    @Test
    fun givenRepositoryWhenDeLanguageFlagIsQueriedThenCorrectFlagIsReturned() {
        val res = sut.getFlag("de")
        assert(res == "\uD83C\uDDE9\uD83C\uDDEA")
    }

    @Test
    fun givenRepositoryWhenFrLanguageFlagIsQueriedThenCorrectFlagIsReturned() {
        val res = sut.getFlag("fr")
        assert(res == "\uD83C\uDDEB\uD83C\uDDF7")
    }

    @Test
    fun givenRepositoryWhenElLanguageFlagIsQueriedThenCorrectFlagIsReturned() {
        val res = sut.getFlag("el")
        assert(res == "\uD83C\uDDEC\uD83C\uDDF7")
    }

    @Test
    fun givenRepositoryWhenUnknownLanguageFlagIsQueriedThenDefaultFlagIsReturned() {
        val res = sut.getFlag("xx")
        assert(res == "\uD83C\uDDEC\uD83C\uDDE7")
    }
}