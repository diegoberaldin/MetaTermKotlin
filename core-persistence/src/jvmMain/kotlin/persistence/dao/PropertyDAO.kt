package persistence.dao

import data.PropertyLevel
import data.PropertyModel
import data.PropertyType
import persistence.entities.PropertyEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class PropertyDAO {
    suspend fun getAll(termbaseId: Int) = newSuspendedTransaction {
        PropertyEntity.select(PropertyEntity.termbaseId eq termbaseId).map {
            it.toPropertyModel()
        }
    }

    suspend fun getById(propertyId: Int) = newSuspendedTransaction {
        PropertyEntity.select(PropertyEntity.id eq propertyId).singleOrNull()?.toPropertyModel()
    }

    suspend fun create(model: PropertyModel) = newSuspendedTransaction {
        PropertyEntity.insert {
            it[termbaseId] = model.termbaseId
            it[name] = model.name
            it[type] = model.type.toInt()
            it[level] = model.level.toInt()
        }[PropertyEntity.id].value
    }

    suspend fun update(model: PropertyModel) = newSuspendedTransaction {
        PropertyEntity.update({ PropertyEntity.id eq model.id }) {
            it[name] = model.name
            it[type] = model.type.toInt()
            it[level] = model.level.toInt()
        }
    }

    suspend fun delete(model: PropertyModel) = newSuspendedTransaction {
        PropertyEntity.deleteWhere { id eq model.id }
    }

    private fun ResultRow.toPropertyModel() = PropertyModel(
        id = this[PropertyEntity.id].value,
        name = this[PropertyEntity.name],
        type = this[PropertyEntity.type].toPropertyType(),
        level = this[PropertyEntity.level].toPropertyLevel(),
        termbaseId = this[PropertyEntity.termbaseId].value,
    )

    private fun Int.toPropertyLevel(): PropertyLevel = when (this) {
        1 -> PropertyLevel.LANGUAGE
        2 -> PropertyLevel.TERM
        else -> PropertyLevel.ENTRY
    }

    private fun PropertyLevel.toInt(): Int = when (this) {
        PropertyLevel.LANGUAGE -> 1
        PropertyLevel.TERM -> 2
        else -> 0
    }

    private fun Int.toPropertyType(): PropertyType = when (this) {
        1 -> PropertyType.IMAGE
        2 -> PropertyType.PICKLIST
        else -> PropertyType.TEXT
    }

    private fun PropertyType.toInt(): Int = when (this) {
        PropertyType.IMAGE -> 1
        PropertyType.PICKLIST -> 2
        else -> 0
    }
}
