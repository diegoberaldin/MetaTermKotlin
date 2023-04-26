package data

import localized

enum class PropertyLevel {
    ENTRY,
    LANGUAGE,
    TERM,
    ALL,
}

fun PropertyLevel.toReadableString(): String = when (this) {
    PropertyLevel.ENTRY -> "property_level_entry".localized()
    PropertyLevel.LANGUAGE -> "property_level_language".localized()
    else -> "property_level_term".localized()
}
