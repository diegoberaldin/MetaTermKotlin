package entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object EntryEntity : IntIdTable() {
    val termbaseId = reference("termbase_id", TermbaseEntity, onDelete = ReferenceOption.CASCADE)
}
