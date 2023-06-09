package common.files

interface FileManager {
    fun getFilePath(vararg components: String): String
}