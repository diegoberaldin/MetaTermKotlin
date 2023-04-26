package data

data class PropertyModel(
    val id: Int = 0,
    val termbaseId: Int = 0,
    val name: String = "",
    val level: PropertyLevel = PropertyLevel.ENTRY,
    val type: PropertyType = PropertyType.TEXT,
    val picklistValues: List<PicklistValueModel> = emptyList(),
)
