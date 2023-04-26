package repository

import localized
import java.util.*

class LanguageNameRepository {

    fun getName(code: String): String = when (code) {
        Locale.FRENCH.language -> "language_french".localized()
        Locale.GERMAN.language -> "language_german".localized()
        Locale.ITALIAN.language -> "language_italian".localized()
        "bg" -> "language_bulgarian".localized()
        "cs" -> "language_czech".localized()
        "da" -> "language_danish".localized()
        "el" -> "language_greek".localized()
        "es" -> "language_spanish".localized()
        "et" -> "language_estonian".localized()
        "fi" -> "language_finnish".localized()
        "ga" -> "language_irish".localized()
        "hr" -> "language_croatian".localized()
        "hu" -> "language_hungarian".localized()
        "lt" -> "language_lithuanian".localized()
        "lv" -> "language_latvian".localized()
        "mt" -> "language_maltese".localized()
        "nl" -> "language_dutch".localized()
        "pl" -> "language_polish".localized()
        "pt" -> "language_portuguese".localized()
        "ro" -> "language_romanian".localized()
        "sk" -> "language_slovak".localized()
        "sl" -> "language_slovenian".localized()
        "sw" -> "language_swedish".localized()
        else -> "language_english".localized()
    }
}
