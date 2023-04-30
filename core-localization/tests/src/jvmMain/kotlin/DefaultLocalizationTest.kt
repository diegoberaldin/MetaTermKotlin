import kotlin.test.BeforeTest
import kotlin.test.Test

class DefaultLocalizationTest {

    private val sut = DefaultLocalization()

    @Test
    fun givenEnglishLanguageWhenQueriedExistingKeyThenTranslationIsReturned() {
        sut.setLanguage("en")
        val key = "app_name"

        val result = sut.get(key)
        assert(key != result)
        assert(result == "MetaTerm")
    }

    @Test
    fun givenEnglishLanguageWhenQueriedNonExistingKeyThenKeyIsReturned() {
        sut.setLanguage("en")
        val key = "app_name_non_existing"

        val result = sut.get(key)
        assert(result == key)
    }

    @Test
    fun givenItalianLanguageWhenQueriedExistingKeyThenTranslationIsReturned() {
        sut.setLanguage("it")
        val key = "app_name"

        val result = sut.get(key)
        assert(key != result)
        assert(result == "MetaTerm_it")
    }

    @Test
    fun givenItalianLanguageWhenQueriedNonExistingKeyThenDefaultIsReturned() {
        sut.setLanguage("it")
        val key = "key_english_only"

        val result = sut.get(key)
        assert(result == "English only")
    }

    @Test
    fun givenItalianLanguageWhenQueriedNonExistingKeyInEveryLanguageThenKeyIsReturned() {
        sut.setLanguage("it")
        val key = "app_name_non_existing"

        val result = sut.get(key)
        assert(result == key)
    }

    @Test
    fun givenUnknownLanguageWhenQueriedExistingKeyThenDefaultIsReturned() {
        sut.setLanguage("xx")
        val key = "app_name"

        val result = sut.get(key)
        assert(key != result)
        assert(result == "MetaTerm")
    }
}