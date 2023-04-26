package dao

import data.PropertyValueModel
import entities.EntryPropertyValueEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class EntryPropertyValueDAO {

    suspend fun getAll(entryId: Int) = newSuspendedTransaction {
        EntryPropertyValueEntity.select(EntryPropertyValueEntity.entryId eq entryId).map {
            it.toPropertyValueModel()
        }
    }

    suspend fun getById(valueId: Int) = newSuspendedTransaction {
        EntryPropertyValueEntity.select(EntryPropertyValueEntity.id eq valueId).singleOrNull()?.toPropertyValueModel()
    }

    suspend fun create(model: PropertyValueModel, entryId: Int) = newSuspendedTransaction {
        EntryPropertyValueEntity.insert {
            it[EntryPropertyValueEntity.entryId] = entryId
            it[value] = model.value
            it[propertyId] = model.propertyId
        }[EntryPropertyValueEntity.id].value
    }

    suspend fun update(model: PropertyValueModel) = newSuspendedTransaction {
        EntryPropertyValueEntity.update({ EntryPropertyValueEntity.id eq model.id }) {
            it[value] = model.value
        }
    }

    suspend fun delete(model: PropertyValueModel) = newSuspendedTransaction {
        EntryPropertyValueEntity.deleteWhere { EntryPropertyValueEntity.id eq model.id }
    }

    private fun ResultRow.toPropertyValueModel() = PropertyValueModel(
        id = this[EntryPropertyValueEntity.id].value,
        value = this[EntryPropertyValueEntity.value],
        propertyId = this[EntryPropertyValueEntity.propertyId].value,
    )
}
