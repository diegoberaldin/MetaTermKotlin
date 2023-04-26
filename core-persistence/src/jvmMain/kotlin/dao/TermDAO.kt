package dao

import data.SearchCriterion
import data.TermModel
import entities.EntryEntity
import entities.EntryPropertyValueEntity
import entities.LanguageEntity
import entities.LanguagePropertyValueEntity
import entities.TermEntity
import entities.TermPropertyValueEntity
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.LikePattern
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class TermDAO {

    suspend fun create(model: TermModel) = newSuspendedTransaction {
        TermEntity.insertIgnore {
            it[lang] = model.lang
            it[lemma] = model.lemma
            it[entryId] = model.entryId
        }[TermEntity.id].value
    }

    suspend fun update(model: TermModel) = newSuspendedTransaction {
        TermEntity.update({ TermEntity.id eq model.id }) {
            it[lemma] = model.lemma
        }
    }

    suspend fun delete(model: TermModel) = newSuspendedTransaction {
        TermEntity.deleteWhere { TermEntity.id eq model.id }
    }

    suspend fun getAll(
        termbaseId: Int,
        mainLang: String? = null,
        entryId: Int? = null,
        criteria: List<SearchCriterion> = emptyList(),
    ) = newSuspendedTransaction {
        val conditions = buildList {
            if (mainLang != null) {
                this += TermEntity.lang eq mainLang
            }
            if (entryId != null) {
                this += TermEntity.entryId eq entryId
            }

            for (criterion in criteria) {
                when (criterion) {
                    is SearchCriterion.FuzzyMatch -> {
                        val search = criterion.text
                        if (search.isEmpty()) {
                            continue
                        }
                        val descriptors = criterion.matching
                        val pattern = LikePattern("%$search%")
                        val orConditions = mutableListOf<Op<Boolean>>()

                        // lemmata search
                        val languageLemmata = descriptors.filter { it.lemma == true }.mapNotNull { it.lang }
                        for (lang in languageLemmata) {
                            orConditions += (TermEntity.lemma.like(pattern)).and(TermEntity.lang eq lang)
                        }

                        // property search
                        val propertyTuples =
                            descriptors.filter { it.propertyId != null }.map { it.propertyId to it.lang }
                        for ((propertyId, lang) in propertyTuples) {
                            if (lang == null) {
                                orConditions += (EntryPropertyValueEntity.propertyId eq propertyId)
                                    .and(EntryPropertyValueEntity.value.like(pattern))
                            } else if (lang == mainLang) {
                                orConditions += (LanguagePropertyValueEntity.propertyId eq propertyId)
                                    .and((LanguageEntity.code eq lang) and (LanguageEntity.termbaseId eq termbaseId))
                                    .and(LanguagePropertyValueEntity.value.like(pattern))
                                orConditions += (TermPropertyValueEntity.propertyId eq propertyId)
                                    .and(TermPropertyValueEntity.value eq search)
                                    .and(TermEntity.lang eq lang)
                            }
                        }

                        if (orConditions.isNotEmpty()) {
                            this += orConditions.fold(Op.FALSE as Op<Boolean>) { acc, it -> acc or it }
                        }
                    }

                    is SearchCriterion.ExactMatch -> {
                        val search = criterion.text
                        if (search.isEmpty()) {
                            continue
                        }
                        val descriptors = criterion.matching
                        val orConditions = mutableListOf<Op<Boolean>>()

                        // lemmata search
                        val languageLemmata = descriptors.filter { it.lemma == true }.mapNotNull { it.lang }
                        for (lang in languageLemmata) {
                            orConditions += (TermEntity.lemma.eq(search)).and(TermEntity.lang eq lang)
                        }

                        // property search
                        val propertyTuples =
                            descriptors.filter { it.propertyId != null }.map { it.propertyId to it.lang }
                        for ((propertyId, lang) in propertyTuples) {
                            if (lang == null) {
                                orConditions += (EntryPropertyValueEntity.propertyId eq propertyId)
                                    .and(EntryPropertyValueEntity.value.eq(search))
                            } else if (lang == mainLang) {
                                orConditions += (LanguagePropertyValueEntity.propertyId eq propertyId)
                                    .and((LanguageEntity.code eq lang) and (LanguageEntity.termbaseId eq termbaseId))
                                    .and(LanguagePropertyValueEntity.value.eq(search))
                                orConditions += (TermPropertyValueEntity.propertyId eq propertyId)
                                    .and(TermPropertyValueEntity.value eq search)
                                    .and(TermEntity.lang eq lang)
                            }
                        }

                        if (orConditions.isNotEmpty()) {
                            this += orConditions.fold(Op.FALSE as Op<Boolean>) { acc, it -> acc or it }
                        }
                    }

                    else -> Unit
                }
            }
        }
        val condition: Op<Boolean> = conditions.fold(Op.TRUE as Op<Boolean>) { acc, it -> acc and it }
        TermEntity
            .join(
                otherTable = TermPropertyValueEntity,
                joinType = JoinType.LEFT,
                onColumn = TermEntity.id,
                otherColumn = TermPropertyValueEntity.termId,
            ).join(
                otherTable = LanguageEntity,
                joinType = JoinType.INNER,
                onColumn = TermEntity.lang,
                otherColumn = LanguageEntity.code,
            ).join(
                otherTable = LanguagePropertyValueEntity,
                joinType = JoinType.LEFT,
                onColumn = LanguageEntity.id,
                otherColumn = LanguagePropertyValueEntity.languageId,
            ).join(
                otherTable = EntryEntity,
                joinType = JoinType.INNER,
                onColumn = TermEntity.entryId,
                otherColumn = EntryEntity.id,
            ).join(
                otherTable = EntryPropertyValueEntity,
                joinType = JoinType.LEFT,
                onColumn = EntryEntity.id,
                otherColumn = EntryPropertyValueEntity.entryId,
            )
            .select(condition)
            .distinctBy { it[TermEntity.id] }
            .map {
                it.toTermModel()
            }
    }

    suspend fun getAll(entryId: Int) = newSuspendedTransaction {
        TermEntity.select(TermEntity.entryId eq entryId).map {
            it.toTermModel()
        }
    }

    suspend fun countAll(termbaseId: Int) = newSuspendedTransaction {
        TermEntity.join(
            otherTable = EntryEntity,
            joinType = JoinType.INNER,
            onColumn = TermEntity.entryId,
            otherColumn = EntryEntity.id,
        ).select(EntryEntity.termbaseId eq termbaseId).count()
    }

    suspend fun countByLanguage(code: String, termbaseId: Int) = newSuspendedTransaction {
        TermEntity.join(
            otherTable = EntryEntity,
            joinType = JoinType.INNER,
            onColumn = TermEntity.entryId,
            otherColumn = EntryEntity.id,
        ).select((EntryEntity.termbaseId eq termbaseId) and (TermEntity.lang eq code)).count()
    }

    suspend fun getById(termId: Int) = newSuspendedTransaction {
        TermEntity.select(TermEntity.id eq termId).singleOrNull()?.toTermModel()
    }

    private fun ResultRow.toTermModel() = TermModel(
        id = this[TermEntity.id].value,
        entryId = this[TermEntity.entryId].value,
        lemma = this[TermEntity.lemma],
        lang = this[TermEntity.lang],
    )
}
