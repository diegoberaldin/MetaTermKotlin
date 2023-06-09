package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object InputDescriptorEntity : IntIdTable() {
    val termbaseId = reference("termbaseId", TermbaseEntity, onDelete = ReferenceOption.CASCADE)
    val propertyId = reference("propertyId", PropertyEntity, onDelete = ReferenceOption.CASCADE).nullable()
    val lemma = bool("lemma").nullable()
    val lang = varchar("lang", 2).nullable()
}
