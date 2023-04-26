package entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PicklistValueEntity : IntIdTable() {
    val propertyId = reference("property_id", PropertyEntity, onDelete = ReferenceOption.CASCADE)
    val value = mediumText("value").uniqueIndex()
}
