package entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object TermEntity : IntIdTable() {
    val entryId = reference("entry_id", EntryEntity, onDelete = ReferenceOption.CASCADE)
    val lemma = mediumText("name")
    val lang = varchar("lang", 2)
}
