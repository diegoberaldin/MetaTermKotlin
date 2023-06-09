package persistence.dao

import data.TermbaseModel
import persistence.entities.TermbaseEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class TermbaseDAO {
    suspend fun create(model: TermbaseModel) = newSuspendedTransaction {
        TermbaseEntity.insertIgnore {
            it[name] = model.name
            it[description] = model.description
        }[TermbaseEntity.id].value
    }

    suspend fun update(model: TermbaseModel) = newSuspendedTransaction {
        TermbaseEntity.update({ TermbaseEntity.id eq model.id }) {
            it[name] = model.name
            it[description] = model.description
        }
    }

    suspend fun delete(model: TermbaseModel) = newSuspendedTransaction {
        TermbaseEntity.deleteWhere {
            TermbaseEntity.id eq model.id
        }
    }

    suspend fun getById(id: Int): TermbaseModel? = newSuspendedTransaction {
        TermbaseEntity.select(TermbaseEntity.id eq id).singleOrNull()?.toTermbaseModel()
    }

    suspend fun getAll(): List<TermbaseModel> = newSuspendedTransaction {
        TermbaseEntity.selectAll().map {
            it.toTermbaseModel()
        }
    }

    private fun ResultRow.toTermbaseModel() = TermbaseModel(
        id = this[TermbaseEntity.id].value,
        name = this[TermbaseEntity.name],
        description = this[TermbaseEntity.description] ?: "",
    )

    fun observeAll() = channelFlow {
        while (true) {
            if (isActive) {
                val value = getAll()
                trySend(value)
                delay(1000)
            } else {
                break
            }
        }
    }
}
