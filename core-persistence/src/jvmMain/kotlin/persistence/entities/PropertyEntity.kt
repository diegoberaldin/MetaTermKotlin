package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PropertyEntity : IntIdTable() {
    val termbaseId = reference("termbase_id", TermbaseEntity, onDelete = ReferenceOption.CASCADE)
    val name = mediumText("name").uniqueIndex()
    val type = integer("type")
    val level = integer("level")
}
