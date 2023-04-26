package log

import files.FileManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class DefaultLogManager(
    fileManager: FileManager,
) : LogManager {

    private val logger: Logger

    init {
        val logDir = fileManager.getFilePath("logs")
        System.setProperty("LOG_DIRECTORY", logDir)
        logger = LoggerFactory.getLogger(DefaultLogManager::class.java)
    }

    override fun trace(message: String) {
        logger.trace(message)
    }

    override fun debug(message: String) {
        logger.debug(message)
    }

    override fun info(message: String) {
        logger.info(message)
    }

    override fun warn(message: String) {
        logger.warn(message)
    }

    override fun error(message: String) {
        logger.error(message)
    }

    override fun exception(message: String, cause: Throwable) {
        logger.error(message, cause)
    }
}