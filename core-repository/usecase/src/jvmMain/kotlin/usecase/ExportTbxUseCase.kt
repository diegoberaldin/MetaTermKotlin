package usecase

import data.EntryModel
import data.TermModel
import data.TermbaseModel
import org.redundent.kotlin.xml.xml
import repository.EntryRepository
import repository.TermRepository
import java.io.File
import java.io.FileWriter

class ExportTbxUseCase(
    private val entryRepository: EntryRepository,
    private val termRepository: TermRepository,
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
                                            "term"(term.lemma)
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
