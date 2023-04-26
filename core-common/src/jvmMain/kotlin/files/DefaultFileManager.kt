package files

import java.io.File
import java.util.*

internal object DefaultFileManager : FileManager {

    private const val DIR_NAME = "MetaTerm"
    private val separator = System.getProperty("file.separator")

    private lateinit var dirPath: String

    init {
        ensureAppDirectory()
    }

    override fun getFilePath(vararg components: String): String {
        return dirPath + separator + components.joinToString(separator = separator)
    }

    private fun ensureAppDirectory() {
        val os = System.getProperty("os.name").uppercase(Locale.getDefault())
        if (os.contains("WIN")) {
            dirPath = System.getenv("APPDATA") + separator + DIR_NAME
        }
        if (os.contains("MAC")) {
            dirPath =
                System.getProperty("user.home") +
                "${separator}Library" + "${separator}Application Support$separator" + DIR_NAME
        }
        if (os.contains("NUX")) {
            dirPath = System.getProperty("user.dir") + separator + ".$DIR_NAME"
        }

        val directory = File(dirPath)
        if (!directory.exists()) {
            directory.mkdir()
        }
    }
}
