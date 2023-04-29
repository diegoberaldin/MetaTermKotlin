package usecase

import data.EntryModel
import data.PropertyLevel
import data.TermModel
import data.TermbaseModel
import org.redundent.kotlin.xml.xml
import repository.EntryRepository
import repository.PropertyRepository
import repository.TermPropertyValueRepository
import repository.TermRepository
import java.io.File
import java.io.FileWriter

private data class TermWithDefinition(val term: TermModel, val definition: String? = null)

class ExportTbxUseCase(
    private val entryRepository: EntryRepository,
    private val termRepository: TermRepository,
    private val propertyRepository: PropertyRepository,
    private val termPropertyValueRepository: TermPropertyValueRepository,
) {

    suspend operator fun invoke(termbase: TermbaseModel, destination: File) {
        val content = getXml(termbase)
        runCatching {
            FileWriter(destination).use {
                it.write(content)
            }
        }
    }

    private suspend fun getXml(termbase: TermbaseModel): String {
        val entries = entryRepository.getAll(termbase.id)
        val properties = propertyRepository.getAll(termbase.id)
        val candidateTermDefinition =
            properties.firstOrNull { it.level == PropertyLevel.TERM && it.name.equals("definition", ignoreCase = true) }
        val entryRegistry = mutableMapOf<EntryModel, Map<String, List<TermWithDefinition>>>()
        for (entry in entries) {
            val languageTermRegistry = mutableMapOf<String, List<TermWithDefinition>>()
            val terms = termRepository.getAll(entryId = entry.id)
            for (term in terms) {
                val lang = term.lang
                if (!languageTermRegistry.containsKey(lang)) {
                    languageTermRegistry[lang] = listOf()
                }
                val oldList = languageTermRegistry[lang] ?: emptyList()
                val definition = if (candidateTermDefinition != null) {
                    val propertyId = candidateTermDefinition.id
                    termPropertyValueRepository.getAll(term.id).firstOrNull { it.propertyId == propertyId }?.value
                } else null
                languageTermRegistry[lang] = oldList + TermWithDefinition(term = term, definition = definition)
            }
            entryRegistry[entry] = languageTermRegistry
        }

        val root = xml("martif") {
            includeXmlProlog = true
            doctype("martif", "SYSTEM", "TBXBasiccoreStructV02.dtd")

            attribute("type", "TBX-Basic")
            attribute("xml:lang", "en-US")
            "martifHeader" {
                "fileDesc" {
                    "titleStmt" {
                        "title"(termbase.name)
                        "note"(termbase.description)
                    }
                }
                "encodingDesc" {
                    "p" {
                        attribute("type", "XCSURI")
                        text("TBXBasicXCSV02.xcs")
                    }
                }
            }
            "text" {
                "body" {
                    for (entry in entryRegistry.keys) {
                        "termEntry" {
                            attribute("id", entry.id)
                            val languages = entryRegistry[entry] ?: emptyMap()
                            for (langCode in languages.keys) {
                                "langSet" {
                                    attribute("xml:lang", langCode)
                                    for (term in languages[langCode] ?: listOf()) {
                                        "tig" {
                                            "term"(term.term.lemma)
                                            if (term.definition != null) {
                                                "descrip" {
                                                    attribute("type", "definition")
                                                    text(term.definition)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return root.toString(true)
    }
}
