package common.keystore

import common.files.FileManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

private object MockFileManager : FileManager {
    private lateinit var file: File

    override fun getFilePath(vararg components: String): String = file.path

    fun setup() {
        file = File.createTempFile("test", ".preferences_pb")
    }

    fun teardown() {
        file.delete()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTemporaryKeyStoreTest {

    private var sut = DefaultTemporaryKeyStore(fileManager = MockFileManager)

    @BeforeTest
    fun setup() {
        MockFileManager.setup()
    }

    @AfterTest
    fun teardown() {
        MockFileManager.teardown()
    }

    @Test
    fun givenEmptyStoreWhenQueriedIntThenDefaultValueIsReturned() = runTest {
        val key = "intKey"
        val retrieved = sut.get(key, 0)
        assert(retrieved == 0)
    }

    @Test
    fun givenSavedIntWhenQueriedWithSameKeyThenCorrectValueIsReturned() = runTest {
        val value = 42
        val key = "intKey"
        sut.save(key, value)

        val retrieved = sut.get(key, 0)
        assert(retrieved == value)
    }

    @Test
    fun givenSavedIntWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() = runTest {
        val value = 42
        val key = "intKey"
        val otherKey = "intKey2"
        sut.save(key, value)

        val retrieved = sut.get(otherKey, 0)
        assert(retrieved == 0)
    }

    @Test
    fun givenEmptyStoreWhenQueriedFloatThenDefaultValueIsReturned() = runTest {
        val key = "intKey"
        val retrieved = sut.get(key, 0f)
        assert(retrieved == 0f)
    }

    @Test
    fun givenSavedFloatWhenQueriedWithSameKeyThenCorrectValueIsReturned() = runTest {
        val value = 42f
        val key = "floatKey"
        sut.save(key, value)

        val retrieved = sut.get(key, 0f)
        assert(retrieved == value)
    }

    @Test
    fun givenSavedFloatWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() = runTest {
        val value = 42f
        val key = "floatKey"
        val otherKey = "floatKey2"
        sut.save(key, value)

        val retrieved = sut.get(otherKey, 0f)
        assert(retrieved == 0f)
    }

    @Test
    fun givenEmptyStoreWhenQueriedDoubleThenDefaultValueIsReturned() = runTest {
        val key = "doubleKey"
        val retrieved = sut.get(key, 0.0)
        assert(retrieved == 0.0)
    }

    @Test
    fun givenSavedDoubleWhenQueriedWithSameKeyThenCorrectValueIsReturned() = runTest {
        val value = 42.0
        val key = "doubleKey"
        sut.save(key, value)

        val retrieved = sut.get(key, 0.0)
        assert(retrieved == value)
    }

    @Test
    fun givenSavedDoubleWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() = runTest {
        val value = 42.0
        val key = "doubleKey"
        val otherKey = "doubleKey2"
        sut.save(key, value)

        val retrieved = sut.get(otherKey, 0.0)
        assert(retrieved == 0.0)
    }

    @Test
    fun givenEmptyStoreWhenQueriedBoolThenDefaultValueIsReturned() = runTest {
        val key = "boolKey"
        val retrieved = sut.get(key, false)
        assert(!retrieved)
    }

    @Test
    fun givenSavedBoolWhenQueriedWithSameKeyThenCorrectValueIsReturned() = runTest {
        val value = true
        val key = "boolKey"
        sut.save(key, value)

        val retrieved = sut.get(key, false)
        assert(retrieved == value)
    }

    @Test
    fun givenSavedBoolWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() = runTest {
        val value = true
        val key = "boolKey"
        val otherKey = "boolKey2"
        sut.save(key, value)

        val retrieved = sut.get(otherKey, false)
        assert(!retrieved)
    }

    @Test
    fun givenEmptyStoreWhenQueriedStringThenDefaultValueIsReturned() = runTest {
        val key = "stringKey"
        val retrieved = sut.get(key, "")
        assert(retrieved == "")
    }

    @Test
    fun givenSavedStoreWhenQueriedWithSameKeyThenCorrectValueIsReturned() = runTest {
        val value = "value"
        val key = "stringKey"
        sut.save(key, value)

        val retrieved = sut.get(key, "")
        assert(retrieved == value)
    }

    @Test
    fun givenSavedStringWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() = runTest {
        val value = "value"
        val key = "stringKey"
        val otherKey = "stringKey2"
        sut.save(key, value)

        val retrieved = sut.get(otherKey, "")
        assert(retrieved == "")
    }
}