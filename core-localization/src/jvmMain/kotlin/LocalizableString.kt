import kotlinx.serialization.Serializable

@Serializable
data class LocalizableString(val key: String, val value: String)
