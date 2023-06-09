package common.log

interface LogManager {
    fun trace(message: String)
    fun debug(message: String)

    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun exception(message: String, cause: Throwable)
}

