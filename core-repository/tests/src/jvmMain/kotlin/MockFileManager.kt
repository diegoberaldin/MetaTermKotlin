import files.FileManager
import java.io.File

internal object MockFileManager : FileManager {

    private lateinit var file: File

    override fun getFilePath(vararg components: String): String = file.path

    fun setup() {
        try {
            file = File.createTempFile("test", ".db")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun teardown() {
        file.delete()
    }
}