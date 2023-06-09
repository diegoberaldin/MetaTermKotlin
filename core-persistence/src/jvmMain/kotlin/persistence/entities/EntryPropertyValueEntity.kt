package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object EntryPropertyValueEntity : IntIdTable() {
    val entryId = reference("entry_id", EntryEntity, onDelete = ReferenceOption.CASCADE)
    val propertyId = reference("property_id", PropertyEntity, onDelete = ReferenceOption.CASCADE)
    val value = largeText("value").nullable()
}
