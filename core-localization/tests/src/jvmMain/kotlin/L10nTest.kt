import kotlin.test.Test

class L10nTest {

    @Test
    fun givenExistingKeyWhenQueriedThenTranslationIsReturned() {
        val key = "app_name"
        val result = key.localized()
        assert(key != result)
        assert(result == "MetaTerm")
    }

    @Test
    fun givenNonExistingKeyWhenQueriedThenKeyIsReturned() {
        val key = "app_name_non_existing"
        val result = key.localized()
        assert(key == result)
    }
}