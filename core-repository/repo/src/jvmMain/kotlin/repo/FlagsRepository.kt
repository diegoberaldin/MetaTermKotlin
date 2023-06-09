package repo

import java.util.*

class FlagsRepository {

    fun getFlag(code: String): String = when (code) {
        Locale.ITALIAN.language -> "\uD83C\uDDEE\uD83C\uDDF9"
        Locale.FRENCH.language -> "\uD83C\uDDEB\uD83C\uDDF7"
        Locale.GERMAN.language -> "\uD83C\uDDE9\uD83C\uDDEA"
        "bg" -> "\uD83C\uDDE7\uD83C\uDDEC"
        "cs" -> "\uD83C\uDDE8\uD83C\uDDFF"
        "da" -> "\uD83C\uDDE9\uD83C\uDDF0"
        "el" -> "\uD83C\uDDEC\uD83C\uDDF7"
        "es" -> "\uD83C\uDDEA\uD83C\uDDF8"
        "et" -> "\uD83C\uDDEA\uD83C\uDDEA"
        "fi" -> "\uD83C\uDDEB\uD83C\uDDEE"
        "ga" -> "\uD83C\uDDEE\uD83C\uDDEA"
        "hr" -> "\uD83C\uDDED\uD83C\uDDF7"
        "hu" -> "\uD83C\uDDED\uD83C\uDDFA"
        "lt" -> "\uD83C\uDDF1\uD83C\uDDF9"
        "lv" -> "\uD83C\uDDF1\uD83C\uDDFB"
        "mt" -> "\uD83C\uDDF2\uD83C\uDDF9"
        "nl" -> "\uD83C\uDDF3\uD83C\uDDF1"
        "pl" -> "\uD83C\uDDF5\uD83C\uDDF1"
        "pt" -> "\uD83C\uDDF5\uD83C\uDDF9"
        "ro" -> "\uD83C\uDDF7\uD83C\uDDF4"
        "sk" -> "\uD83C\uDDF8\uD83C\uDDF0"
        "sl" -> "\uD83C\uDDF8\uD83C\uDDEE"
        "sw" -> "\uD83C\uDDF8\uD83C\uDDEA"
        else -> "\uD83C\uDDEC\uD83C\uDDE7"
    }
}
