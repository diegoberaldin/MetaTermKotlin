package files

interface FileManager {
    fun getFilePath(vararg components: String): String
}