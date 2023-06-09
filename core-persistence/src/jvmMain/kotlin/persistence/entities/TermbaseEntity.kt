package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object TermbaseEntity : IntIdTable() {
    val name = mediumText("name").uniqueIndex()
    val description = largeText("description").nullable()
}
