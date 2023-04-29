package usecase

import data.EntryModel
import data.TermModel
import data.TermbaseModel
import repository.EntryRepository
import repository.LanguageRepository
import repository.TermRepository
import java.io.File

class ImportCsvUseCase(
    private val entryRepository: EntryRepository,
    private val termRepository: TermRepository,
    private val languageRepository: LanguageRepository,
) {

    suspend operator fun invoke(termbase: TermbaseModel, source: File) {
        val entryRegistry = getEntryRegistry(source)

        for (key in entryRegistry.keys) {
            val entry = EntryModel(termbaseId = termbase.id)
            val entryId = entryRepository.create(entry)
            for (lang in entryRegistry[key]?.keys ?: emptyList()) {
                val allowedCodes = languageRepository.getAll(termbase.id).map { it.code }
                if (lang !in allowedCodes) {
                    continue
                }

                for (lemma in entryRegistry[key]?.get(lang) ?: emptyList()) {
                    val term = TermModel(entryId = entryId, lang = lang, lemma = lemma)
                    termRepository.create(term)
                }
            }
        }
    }

    private fun getEntryRegistry(source: File): Map<Int, Map<String, List<String>>> {
        val res = mutableMapOf<Int, Map<String, List<String>>>()
        val lines = source.readLines()
        val langCodes = lines.firstOrNull()?.split(",")?.map { it.trim() } ?: emptyList()
        if (langCodes.isEmpty()) {
            return res
        }

        // build clusters of lines that belong to the same entry
        val clusters = mutableListOf<Set<Int>>()
        val splitLines = lines
            .filterIndexed { i, _ -> i > 0 }
            .map { it.split(",").map { e -> e.trim() } }
        for (columnIndex in langCodes.indices) {
            val termsForLanguages = splitLines.map { it[columnIndex] }
            val registry = mutableMapOf<String, List<Int>>()
            termsForLanguages.forEachIndexed { index, s ->
                if (!registry.containsKey(s)) {
                    registry[s] = emptyList()
                }
                registry[s] = (registry[s] ?: emptyList()) + index
            }

            for (c in registry.values) {
                val index = clusters.indexOfFirst { it.intersect(c.toSet()).isNotEmpty() }
                if (index >= 0) {
                    clusters[index] = clusters[index].union(c)
                } else {
                    clusters.add(c.toSet())
                }
            }
        }

        // create the registry with all the lemmata for language or the entry
        for (index in clusters.indices) {
            val map = mutableMapOf<String, List<String>>()
            val cluster = clusters[index]
            for (rowIndex in cluster) {
                for (langIndex in langCodes.indices) {
                    val langCode = langCodes[langIndex]
                    val lemma = splitLines[rowIndex][langIndex]
                    if (!map.containsKey(langCode)) {
                        map[langCode] = emptyList()
                    }
                    val existingList = map[langCode] ?: emptyList()
                    if (!existingList.contains(lemma)) {
                        map[langCode] = existingList + lemma
                    }
                }
            }
            res[index] = map
        }

        return res
    }
}
