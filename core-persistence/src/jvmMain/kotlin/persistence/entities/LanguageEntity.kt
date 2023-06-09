package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object LanguageEntity : IntIdTable() {
    val code = varchar("code", 2)
    val termbaseId = reference("termbase_id", TermbaseEntity, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(code, termbaseId)
    }
}
