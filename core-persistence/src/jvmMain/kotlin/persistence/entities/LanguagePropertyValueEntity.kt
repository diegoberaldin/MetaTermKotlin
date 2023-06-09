package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object LanguagePropertyValueEntity : IntIdTable() {
    val languageId = reference("language_id", LanguageEntity, onDelete = ReferenceOption.CASCADE)
    val entryId = reference("entry_id", EntryEntity, onDelete = ReferenceOption.CASCADE)
    val propertyId = reference("property_id", PropertyEntity, onDelete = ReferenceOption.CASCADE)
    val value = largeText("value").nullable()
}
