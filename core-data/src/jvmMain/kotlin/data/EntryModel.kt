package data

data class EntryModel(
    val id: Int = 0,
    val termbaseId: Int = 0,
    @Transient val new: Boolean = false,
)
