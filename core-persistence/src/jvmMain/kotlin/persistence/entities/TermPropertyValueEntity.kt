package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object TermPropertyValueEntity : IntIdTable() {
    val termId = reference("term_id", TermEntity, onDelete = ReferenceOption.CASCADE)
    val propertyId = reference("property_id", PropertyEntity, onDelete = ReferenceOption.CASCADE)
    val value = largeText("value").nullable()
}
