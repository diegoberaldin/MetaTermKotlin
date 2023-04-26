package data

import localized

enum class PropertyType {
    TEXT,
    IMAGE,
    PICKLIST,
}

fun PropertyType.toReadableString(): String = when (this) {
    PropertyType.PICKLIST -> "definition_model_type_picklist".localized()
    PropertyType.IMAGE -> "definition_model_type_image".localized()
    else -> "definition_model_type_text".localized()
}
