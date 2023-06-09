package usecase

import data.EntryModel
import data.TermModel
import data.TermbaseModel
import repo.EntryRepository
import repo.LanguageRepository
import repo.TermRepository
import java.io.File
import java.io.FileWriter
import kotlin.math.max

class ExportCsvUseCase(
    private val entryRepository: EntryRepository,
    private val termRepository: TermRepository,
    private val languageRepository: LanguageRepository,
) {

    suspend operator fun invoke(termbase: TermbaseModel, destination: File) {
        val content = getCsv(termbase)
        runCatching {
            FileWriter(destination).use {
                it.write(content)
            }
        }
    }

    private suspend fun getCsv(termbase: TermbaseModel): String {
        val entries = entryRepository.getAll(termbase.id)
        val entryRegistry = mutableMapOf<EntryModel, Map<String, List<TermModel>>>()
        for (entry in entries) {
            val languageTermRegistry = mutableMapOf<String, List<TermModel>>()
            val terms = termRepository.getAll(entryId = entry.id)
            for (term in terms) {
                val lang = term.lang
                if (!languageTermRegistry.containsKey(lang)) {
                    languageTermRegistry[lang] = listOf()
                }
                val oldList = languageTermRegistry[lang] ?: emptyList()
                languageTermRegistry[lang] = oldList + term
            }
            entryRegistry[entry] = languageTermRegistry
        }
        val langCodes = languageRepository.getAll(termbase.id).map { it.code }

        return buildString {
            // header
            append(langCodes.joinToString(", ")).append("\n")

            // body
            for (entry in entryRegistry.keys) {
                val mapCodeListTerm = entryRegistry[entry] ?: emptyMap()
                val numRows = mapCodeListTerm.values.fold(0) { acc, it -> max(acc, it.size) }
                for (i in 0 until numRows) {
                    for (lang in langCodes) {
                        val terms = mapCodeListTerm[lang] ?: emptyList()
                        val defaultTerm = terms.firstOrNull() ?: TermModel()
                        val term = terms.getOrElse(i) { defaultTerm }
                        append(term.lemma)
                        if (lang != langCodes.last()) {
                            append(", ")
                        }
                    }
                    append("\n")
                }
            }
            toString()
        }
    }
}
