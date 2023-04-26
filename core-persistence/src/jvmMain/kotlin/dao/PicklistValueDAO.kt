package dao

import data.PicklistValueModel
import entities.PicklistValueEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PicklistValueDAO {

    suspend fun deleteAll(propertyId: Int) = newSuspendedTransaction {
        PicklistValueEntity.deleteWhere { PicklistValueEntity.propertyId eq propertyId }
    }

    suspend fun getAll(propertyId: Int) = newSuspendedTransaction {
        PicklistValueEntity.select(PicklistValueEntity.propertyId eq propertyId).map {
            it.toPicklistValueModel()
        }
    }

    suspend fun insertAll(values: List<PicklistValueModel>, propertyId: Int) = newSuspendedTransaction {
        PicklistValueEntity.batchInsert(values, ignore = true) {
            set(PicklistValueEntity.value, it.value)
            set(PicklistValueEntity.propertyId, propertyId)
        }
    }

    private fun ResultRow.toPicklistValueModel() = PicklistValueModel(
        value = this[PicklistValueEntity.value],
    )
}
