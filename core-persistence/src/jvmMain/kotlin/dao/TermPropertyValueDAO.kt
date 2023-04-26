package dao

import data.PropertyValueModel
import entities.TermPropertyValueEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class TermPropertyValueDAO {

    suspend fun getAll(termId: Int) = newSuspendedTransaction {
        TermPropertyValueEntity.select(TermPropertyValueEntity.termId eq termId).map {
            it.toPropertyValueModel()
        }
    }

    suspend fun getById(valueId: Int) = newSuspendedTransaction {
        TermPropertyValueEntity.select(TermPropertyValueEntity.id eq valueId).singleOrNull()?.toPropertyValueModel()
    }

    suspend fun create(model: PropertyValueModel, termId: Int) = newSuspendedTransaction {
        TermPropertyValueEntity.insert {
            it[TermPropertyValueEntity.termId] = termId
            it[value] = model.value
            it[propertyId] = model.propertyId
        }[TermPropertyValueEntity.id].value
    }

    suspend fun update(model: PropertyValueModel) = newSuspendedTransaction {
        TermPropertyValueEntity.update({ TermPropertyValueEntity.id eq model.id }) {
            it[value] = model.value
        }
    }

    suspend fun delete(model: PropertyValueModel) = newSuspendedTransaction {
        TermPropertyValueEntity.deleteWhere { id eq model.id }
    }

    private fun ResultRow.toPropertyValueModel() = PropertyValueModel(
        id = this[TermPropertyValueEntity.id].value,
        value = this[TermPropertyValueEntity.value],
        propertyId = this[TermPropertyValueEntity.propertyId].value,
    )
}
