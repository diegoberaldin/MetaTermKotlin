package common.files

import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test


class DefaultFileManagerTest {

    private val sut = DefaultFileManager
    private var testFile: File? = null

    @AfterTest
    fun teardown() {
        testFile?.also {
            it.delete()
        }
    }

    @Test
    fun whenInvokedWithFileNameThenFileCanBeRead() {
        val path = sut.getFilePath("test.txt")
        testFile = File(path)
        testFile?.also {
            assert(!it.exists())
            assert(it.createNewFile())
            assert(it.exists())
            assert(it.canRead())
        }
    }
    @Test
    fun whenInvokedWithFileNameThenFileCanBeWritten() {
        val path = sut.getFilePath("test.txt")
        testFile = File(path)
        testFile?.also {
            assert(!it.exists())
            assert(it.createNewFile())
            assert(it.exists())
            assert(it.canWrite())
        }
    }
}