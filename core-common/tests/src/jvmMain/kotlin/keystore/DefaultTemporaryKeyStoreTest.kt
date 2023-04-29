package keystore

import files.FileManager
import kotlinx.coroutines.runBlocking
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
    fun givenEmptyStoreWhenQueriedIntThenDefaultValueIsReturned() {
        val key = "intKey"
        runBlocking {
            val retrieved = sut.get(key, 0)
            assert(retrieved == 0)
        }
    }

    @Test
    fun givenSavedIntWhenQueriedWithSameKeyThenCorrectValueIsReturned() {
        val value = 42
        val key = "intKey"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(key, 0)
            assert(retrieved == value)
        }
    }

    @Test
    fun givenSavedIntWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() {
        val value = 42
        val key = "intKey"
        val otherKey = "intKey2"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(otherKey, 0)
            assert(retrieved == 0)
        }
    }

    @Test
    fun givenEmptyStoreWhenQueriedFloatThenDefaultValueIsReturned() {
        val key = "intKey"
        runBlocking {
            val retrieved = sut.get(key, 0f)
            assert(retrieved == 0f)
        }
    }

    @Test
    fun givenSavedFloatWhenQueriedWithSameKeyThenCorrectValueIsReturned() {
        val value = 42f
        val key = "floatKey"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(key, 0f)
            assert(retrieved == value)
        }
    }

    @Test
    fun givenSavedFloatWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() {
        val value = 42f
        val key = "floatKey"
        val otherKey = "floatKey2"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(otherKey, 0f)
            assert(retrieved == 0f)
        }
    }

    @Test
    fun givenEmptyStoreWhenQueriedDoubleThenDefaultValueIsReturned() {
        val key = "doubleKey"
        runBlocking {
            val retrieved = sut.get(key, 0.0)
            assert(retrieved == 0.0)
        }
    }

    @Test
    fun givenSavedDoubleWhenQueriedWithSameKeyThenCorrectValueIsReturned() {
        val value = 42.0
        val key = "doubleKey"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(key, 0.0)
            assert(retrieved == value)
        }
    }

    @Test
    fun givenSavedDoubleWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() {
        val value = 42.0
        val key = "doubleKey"
        val otherKey = "doubleKey2"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(otherKey, 0.0)
            assert(retrieved == 0.0)
        }
    }

    @Test
    fun givenEmptyStoreWhenQueriedBoolThenDefaultValueIsReturned() {
        val key = "boolKey"
        runBlocking {
            val retrieved = sut.get(key, false)
            assert(!retrieved)
        }
    }

    @Test
    fun givenSavedBoolWhenQueriedWithSameKeyThenCorrectValueIsReturned() {
        val value = true
        val key = "boolKey"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(key, false)
            assert(retrieved == value)
        }
    }

    @Test
    fun givenSavedBoolWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() {
        val value = true
        val key = "boolKey"
        val otherKey = "boolKey2"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(otherKey, false)
            assert(!retrieved)
        }
    }

    @Test
    fun givenEmptyStoreWhenQueriedStringThenDefaultValueIsReturned() {
        val key = "stringKey"
        runBlocking {
            val retrieved = sut.get(key, "")
            assert(retrieved == "")
        }
    }

    @Test
    fun givenSavedStoreWhenQueriedWithSameKeyThenCorrectValueIsReturned() {
        val value = "value"
        val key = "stringKey"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(key, "")
            assert(retrieved == value)
        }
    }

    @Test
    fun givenSavedStringWhenQueriedWithDifferentKeyThenDefaultValueIsReturned() {
        val value = "value"
        val key = "stringKey"
        val otherKey = "stringKey2"
        runBlocking {
            sut.save(key, value)

            val retrieved = sut.get(otherKey, "")
            assert(retrieved == "")
        }
    }
}